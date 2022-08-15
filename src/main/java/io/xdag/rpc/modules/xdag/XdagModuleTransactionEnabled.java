/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020-2030 The XdagJ Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.xdag.rpc.modules.xdag;

import static io.xdag.core.XdagField.FieldType.XDAG_FIELD_IN;
import static io.xdag.rpc.ErrorCode.ERR_BALANCE_NOT_ENOUGH;
import static io.xdag.rpc.ErrorCode.ERR_PARAM_INVALID;
import static io.xdag.rpc.ErrorCode.ERR_TO_ADDRESS_INVALID;
import static io.xdag.rpc.ErrorCode.ERR_VALUE_INVALID;
import static io.xdag.rpc.ErrorCode.ERR_WALLET_UNLOCK;
import static io.xdag.rpc.ErrorCode.SUCCESS;
import static io.xdag.utils.BasicUtils.address2Hash;
import static io.xdag.utils.BasicUtils.amount2xdag;
import static io.xdag.utils.BasicUtils.xdag2amount;

import com.google.common.collect.Maps;
import io.xdag.Kernel;
import io.xdag.core.Address;
import io.xdag.core.Block;
import io.xdag.core.BlockWrapper;
import io.xdag.core.ImportResult;
import io.xdag.rpc.Web3.CallArguments;
import io.xdag.rpc.dto.ProcessResult;
import io.xdag.utils.BasicUtils;
import io.xdag.utils.exception.XdagOverFlowException;
import io.xdag.wallet.Wallet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.bytes.MutableBytes32;
import org.hyperledger.besu.crypto.KeyPair;

public class XdagModuleTransactionEnabled extends XdagModuleTransactionBase {

    private final Kernel kernel;

    public XdagModuleTransactionEnabled(Kernel kernel) {
        super(kernel);
        this.kernel = kernel;
    }

    @Override
    public String storeTransaction(String _from, String _to, String _value, String _nonce, String _chainId,
            String _gasPrice, String _remark) {
        String result = super.storeTransaction(_from, _to, _value, _nonce, _chainId,
                _gasPrice, _remark);
        return result;
    }

    @Override
    public String sendRawTransaction(String rawData) {
        String result = super.sendRawTransaction(rawData);
        return result;
    }

    @Override
    public Object personalSendTransaction(CallArguments args, String passphrase) {

        String from = args.from;
        String to = args.to;
        String value = args.value;
        String remark = args.remark;

        ProcessResult result = ProcessResult.builder().code(SUCCESS.code()).build();

        Bytes32 hash = checkParam(from, to, value, remark, result);
        if (result.getCode() != SUCCESS.code()) {
            return result.getErrMsg();
        }
        checkPassword(passphrase, result);
        if (result.getCode() != SUCCESS.code()) {
            return result.getErrMsg();
        }

        // do xfer
        double amount = BasicUtils.getDouble(value);
        doXfer(amount, hash, remark, result);

        if (result.getCode() != SUCCESS.code()) {
            return result.getErrMsg();
        } else {
            return result.getResInfo();
        }
    }

    public void doXfer(double sendValue, Bytes32 toAddress, String remark, ProcessResult processResult) {
        long amount = 0;
        try {
            amount = xdag2amount(sendValue);
        } catch (XdagOverFlowException e) {
            processResult.setCode(ERR_PARAM_INVALID.code());
            processResult.setErrMsg(ERR_PARAM_INVALID.msg());
            return;
        }
        MutableBytes32 to = MutableBytes32.create();
        // System.arraycopy(address, 8, to, 8, 24);
        to.set(8, toAddress.slice(8, 24));

        // 待转账余额
        AtomicLong remain = new AtomicLong(amount);
        // 转账输入
        Map<Address, KeyPair> ourBlocks = Maps.newHashMap();

        // our block select
        kernel.getBlockStore().fetchOurBlocks(pair -> {
            int index = pair.getKey();
            Block block = pair.getValue();
            if (remain.get() <= block.getInfo().getAmount()) {
                ourBlocks.put(new Address(block.getHashLow(), XDAG_FIELD_IN, remain.get()),
                        kernel.getWallet().getAccounts().get(index));
                remain.set(0);
                return true;
            } else {
                if (block.getInfo().getAmount() > 0) {
                    remain.set(remain.get() - block.getInfo().getAmount());
                    ourBlocks.put(new Address(block.getHashLow(), XDAG_FIELD_IN, block.getInfo().getAmount()),
                            kernel.getWallet().getAccounts().get(index));
                    return false;
                }
                return false;
            }
        });

        // 余额不足
        if (remain.get() > 0) {
            processResult.setCode(ERR_BALANCE_NOT_ENOUGH.code());
            processResult.setErrMsg(ERR_BALANCE_NOT_ENOUGH.msg());
            return;
        }
        List<String> resInfo = new ArrayList<>();
        // create transaction
        List<BlockWrapper> txs = kernel.getWallet().createTransactionBlock(ourBlocks, to, remark);
        for (BlockWrapper blockWrapper : txs) {
            ImportResult result = kernel.getSyncMgr().validateAndAddNewBlock(blockWrapper);
            if (result == ImportResult.IMPORTED_BEST || result == ImportResult.IMPORTED_NOT_BEST) {
                kernel.getChannelMgr().sendNewBlock(blockWrapper);
                resInfo.add(BasicUtils.hash2Address(blockWrapper.getBlock().getHashLow()));
            }
        }

        processResult.setCode(SUCCESS.code());
        processResult.setResInfo(resInfo);
    }

    private Bytes32 checkParam(String from, String to, String value, String remark, ProcessResult processResult) {
        Bytes32 hash = null;
        try {
            double amount = BasicUtils.getDouble(value);
            if (amount < 0) {
                processResult.setCode(ERR_VALUE_INVALID.code());
                processResult.setErrMsg(ERR_VALUE_INVALID.msg());
                return null;
            }

            // check whether to is exist in blockchain
            if (to.length() == 32) {
                hash = Bytes32.wrap(address2Hash(to));
            } else {
                hash = Bytes32.wrap(BasicUtils.getHash(to));
            }
            if (hash == null) {
                processResult.setCode(ERR_TO_ADDRESS_INVALID.code());
                processResult.setErrMsg(ERR_TO_ADDRESS_INVALID.msg());
            } else {
                if (kernel.getBlockchain().getBlockByHash(Bytes32.wrap(hash), false) == null) {
                    processResult.setCode(ERR_TO_ADDRESS_INVALID.code());
                    processResult.setErrMsg(ERR_TO_ADDRESS_INVALID.msg());
                }
            }

        } catch (NumberFormatException e) {
            processResult.setCode(e.hashCode());
            processResult.setErrMsg(e.getMessage());
        }
        return hash;
    }

    private void checkPassword(String passphrase, ProcessResult result) {
        Wallet wallet = new Wallet(kernel.getConfig());
        try {
            boolean res = wallet.unlock(passphrase);
            if (!res) {
                result.setCode(ERR_WALLET_UNLOCK.code());
                result.setErrMsg(ERR_WALLET_UNLOCK.msg());
            }
        } catch (Exception e) {
            result.setCode(e.hashCode());
            result.setErrMsg(e.getMessage());
        }
    }
}
