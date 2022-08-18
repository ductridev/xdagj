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

package io.xdag.net;

import io.xdag.Kernel;
import io.xdag.config.Config;
import io.xdag.config.DevnetConfig;
import io.xdag.net.manager.XdagChannelManager;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class XdagChannelManagerTest {

    Config config = new DevnetConfig();
    Kernel kernel;

    @Before
    public void setUp() throws Exception {
        String[] list = new String[]{"127.0.0.1:1001", "127.0.0.1:1002"};
        List<InetSocketAddress> addressList = new ArrayList<>();
        for (String address : list) {
            addressList.add(new InetSocketAddress(address.split(":")[0],Integer.parseInt(address.split(":")[1])));
        }
        config.getNodeSpec().setWhiteIPList(addressList);
        kernel = new Kernel(config);
    }

    @Test
    public void testIp() {
        XdagChannelManager channelManager = new XdagChannelManager(kernel);
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 1001);
        assertTrue(channelManager.isAcceptable(inetSocketAddress));
    }
}
