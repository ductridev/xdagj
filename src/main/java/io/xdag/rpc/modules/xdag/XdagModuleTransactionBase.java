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

import io.xdag.Kernel;
import io.xdag.core.Address;
import io.xdag.core.Block;
import io.xdag.core.BlockWrapper;
import io.xdag.core.ImportResult;
import io.xdag.core.XdagBlock;
import io.xdag.rpc.Web3;
import io.xdag.rpc.Web3.CallArguments;
import io.xdag.utils.BasicUtils;
import io.xdag.utils.XdagTime;
import io.xdag.wallet.Wallet;

import static io.xdag.core.XdagField.FieldType.*;
import static io.xdag.utils.BasicUtils.address2Hash;
import static io.xdag.utils.BasicUtils.xdag2amount;
import static io.xdag.config.Constants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.bytes.MutableBytes32;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.hyperledger.besu.crypto.KeyPair;

public class XdagModuleTransactionBase implements XdagModuleTransaction {

    protected static final Logger logger = LoggerFactory.getLogger(XdagModuleTransactionBase.class);

    private final Kernel kernel;

    public XdagModuleTransactionBase(Kernel kernel) {

        this.kernel = kernel;
    }

    @Override
    public synchronized String sendTransaction(Web3.CallArguments args) {
        // 1. process args
        // byte[] from = Hex.decode(args.from);
        // byte[] to = Hex.decode(args.to);
        // BigInteger value = args.value != null ?
        // TypeConverter.stringNumberAsBigInt(args.value) : BigInteger.ZERO;
        // 2. create a transaction
        // 3. try to add blockchain
        return null;
    }

    @Override
    public Object storeTransaction(String _paymentID, String admin, String _value, String _remark) {
        Bytes32 hash;
        double amount = BasicUtils.getDouble(_value);

        String remark = _remark != null ? _remark : null;

        if (admin.length() == 32) {
            hash = Bytes32.wrap(address2Hash(admin));
        } else {
            hash = Bytes32.wrap(BasicUtils.getHash(admin));
        }
        if (hash == null) {
            return "No Address";
        }

        // store through xfer
        MutableBytes32 key = MutableBytes32.create();
        key.set(8, Objects.requireNonNull(hash).slice(8, 24));
        if (kernel.getBlockchain().getBlockByHash(Bytes32.wrap(key), false) == null) {
            // if (kernel.getAccountStore().getAccountBlockByHash(hash, false) == null) {
            return "Incorrect address";
        }

        Wallet wallet = new Wallet(kernel.getConfig());
        if (!wallet.unlock("ductridev")) {
            return "The password is incorrect";
        }
        storeWithXfer(amount, hash, remark);

        return "";
    }

    @Override
    public String sendRawTransaction(String rawData) {

        // 1. build transaction
        // 2. try to add blockchain

        Block block = new Block(new XdagBlock(Hex.decode(rawData)));
        kernel.getSyncMgr().importBlock(
                new BlockWrapper(block, kernel.getConfig().getNodeSpec().getTTL()));
        return BasicUtils.hash2Address(block.getHash());
    }

    @Override
    public Object personalSendTransaction(CallArguments args, String passphrase) {
        return null;
    }

    private String storeWithXfer(double sendAmount, Bytes32 address, String remark) {
        StringBuilder str = new StringBuilder();
        str.append("Transaction :{ ").append("\n");

        long amount = xdag2amount(sendAmount);
        MutableBytes32 to = MutableBytes32.create();
        // System.arraycopy(address, 8, to, 8, 24);
        to.set(8, address.slice(8, 24));

        // 待转账余额
        AtomicLong remain = new AtomicLong(amount);
        // 转账输入
        Map<Address, KeyPair> ourBlocks = Maps.newHashMap();

        // our block select
        kernel.getBlockStore().fetchOurBlocks(pair -> {
            int index = pair.getKey();
            Block block = pair.getValue();
            if (XdagTime.getCurrentEpoch() < XdagTime.getEpoch(block.getTimestamp()) + 2 * CONFIRMATIONS_COUNT) {
                System.out.println(BasicUtils.hash2Address(block.getHash()));
                return false;
            }
            ourBlocks.put(new Address(block.getHashLow(), XDAG_FIELD_IN, remain.get()),
                    kernel.getWallet().getAccounts().get(index));
            remain.set(0);
            return true;
            // if (remain.get() <= block.getInfo().getAmount()) {
            // ourBlocks.put(new Address(block.getHashLow(), XDAG_FIELD_IN, remain.get()),
            // kernel.getWallet().getAccounts().get(index));
            // remain.set(0);
            // return true;
            // } else {
            // if (block.getInfo().getAmount() > 0) {
            // remain.set(remain.get() - block.getInfo().getAmount());
            // ourBlocks.put(new Address(block.getHashLow(), XDAG_FIELD_IN,
            // block.getInfo().getAmount()),
            // kernel.getWallet().getAccounts().get(index));
            // return false;
            // }
            // return false;
            // }
        });

        // 余额不足
        if (remain.get() > 0) {
            return "Balance not enough.";
        }

        // 生成多个交易块
        List<BlockWrapper> txs = createTransactionBlock(ourBlocks, to, remark);
        for (BlockWrapper blockWrapper : txs) {
            ImportResult result = kernel.getSyncMgr().validateAndAddNewBlock(blockWrapper);
            if (result == ImportResult.IMPORTED_BEST || result == ImportResult.IMPORTED_NOT_BEST) {
                kernel.getChannelMgr().sendNewBlock(blockWrapper);
                str.append(BasicUtils.hash2Address(blockWrapper.getBlock().getHashLow())).append("\n");
            }
        }

        return str.append("}, it will take several minutes to complete the transaction.").toString();

    }

    private List<BlockWrapper> createTransactionBlock(Map<Address, KeyPair> ourKeys, Bytes32 to, String remark) {
        // 判断是否有remark
        int hasRemark = remark == null ? 0 : 1;

        List<BlockWrapper> res = new ArrayList<>();

        // 遍历ourKeys 计算每个区块最多能放多少个
        // int res = 1 + pairs.size() + to.size() + 3*keys.size() + (defKeyIndex == -1 ?
        // 2 : 0);
        LinkedList<Map.Entry<Address, KeyPair>> stack = new LinkedList<>(ourKeys.entrySet());

        // 每次创建区块用到的keys
        Map<Address, KeyPair> keys = new HashMap<>();
        // 保证key的唯一性
        Set<KeyPair> keysPerBlock = new HashSet<>();
        // 放入defkey
        keysPerBlock.add(kernel.getWallet().getDefKey());

        // base count a block <header + send address + defKey signature>
        int base = 1 + 1 + 2 + hasRemark;
        long amount = 0;

        while (stack.size() > 0) {
            Map.Entry<Address, KeyPair> key = stack.peek();
            base += 1;
            int originSize = keysPerBlock.size();
            keysPerBlock.add(key.getValue());
            // 说明新增加的key没有重复
            if (keysPerBlock.size() > originSize) {
                // 一个字段公钥加两个字段签名
                base += 3;
            }
            // 可以将该输入 放进一个区块
            if (base < 16) {
                amount += key.getKey().getAmount().longValue();
                keys.put(key.getKey(), key.getValue());
                stack.poll();
            } else {
                res.add(createTransaction(to, amount, keys, remark));
                // 清空keys，准备下一个
                keys = new HashMap<>();
                keysPerBlock = new HashSet<>();
                keysPerBlock.add(kernel.getWallet().getDefKey());
                base = 1 + 1 + 2 + hasRemark;
                amount = 0;
            }
        }
        if (keys.size() != 0) {
            res.add(createTransaction(to, amount, keys, remark));
        }

        return res;
    }

    private BlockWrapper createTransaction(Bytes32 to, long amount, Map<Address, KeyPair> keys, String remark) {

        List<Address> tos = Lists.newArrayList(new Address(to, XDAG_FIELD_OUT, amount));

        Block block = kernel.getBlockchain().createNewBlock(new HashMap<>(keys), tos, false, remark);

        if (block == null) {
            return null;
        }

        KeyPair defaultKey = kernel.getWallet().getDefKey();

        boolean isdefaultKey = false;
        // 签名
        for (KeyPair ecKey : Set.copyOf(new HashMap<>(keys).values())) {
            if (ecKey.equals(defaultKey)) {
                isdefaultKey = true;
                block.signOut(ecKey);
            } else {
                block.signIn(ecKey);
            }
        }
        // 如果默认密钥被更改，需要重新对输出签名签属
        if (!isdefaultKey) {
            block.signOut(kernel.getWallet().getDefKey());
        }

        return new BlockWrapper(block, kernel.getConfig().getNodeSpec().getTTL());
    }
}
