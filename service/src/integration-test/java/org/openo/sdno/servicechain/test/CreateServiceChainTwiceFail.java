/*
 * Copyright 2016 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openo.sdno.servicechain.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openo.baseservice.remoteservice.exception.ServiceException;
import org.openo.sdno.exception.HttpCode;
import org.openo.sdno.overlayvpn.errorcode.ErrorCode;
import org.openo.sdno.servicechain.mocoserver.SbiAdapterSuccessServer;
import org.openo.sdno.servicechain.util.HttpRest;
import org.openo.sdno.testframework.checker.IChecker;
import org.openo.sdno.testframework.http.model.HttpModelUtils;
import org.openo.sdno.testframework.http.model.HttpRequest;
import org.openo.sdno.testframework.http.model.HttpResponse;
import org.openo.sdno.testframework.http.model.HttpRquestResponse;
import org.openo.sdno.testframework.replace.PathReplace;
import org.openo.sdno.testframework.testmanager.TestManager;

/**
 * CreateServiceChainTwiceFail test class.<br>
 * 
 * @author
 * @version SDNO 0.5 August 22, 2016
 */
public class CreateServiceChainTwiceFail extends TestManager {

    private static SbiAdapterSuccessServer sbiAdapterServer1 = new SbiAdapterSuccessServer();

    private static final String CREATE_SERVICECHAIN_SUCCESS_TESTCASE =
            "src/integration-test/resources/testcase/createservicechainsuccess1.json";

    private static final String DELETE_SERVICECHAIN_SUCCESS_TESTCASE =
            "src/integration-test/resources/testcase/deleteservicechainsuccess1.json";

    private int times = 0;

    @BeforeClass
    public static void setup() throws ServiceException {
        sbiAdapterServer1.start();
    }

    @AfterClass
    public static void tearDown() throws ServiceException {
        sbiAdapterServer1.stop();
    }

    @Test
    public void testCreateServiceChainTwice() throws ServiceException {
        try {
            // create first time
            times = 1;
            HttpRquestResponse httpCreateObject =
                    HttpModelUtils.praseHttpRquestResponseFromFile(CREATE_SERVICECHAIN_SUCCESS_TESTCASE);
            HttpRequest createRequest = httpCreateObject.getRequest();
            execTestCase(createRequest, new CheckerCreateTwice());
            // create again
            times = 2;
            execTestCase(createRequest, new CheckerCreateTwice());
        } finally {
            // clear data
            HttpRquestResponse deleteHttpObject =
                    HttpModelUtils.praseHttpRquestResponseFromFile(DELETE_SERVICECHAIN_SUCCESS_TESTCASE);
            HttpRequest deleteRequest = deleteHttpObject.getRequest();
            deleteRequest.setUri(PathReplace.replaceUuid("uuid", deleteRequest.getUri(), "servicechainId1"));
            HttpRest.doSend(deleteRequest);
        }
    }

    private class CheckerCreateTwice implements IChecker {

        @Override
        public boolean check(HttpResponse response) {
            if(1 == times) {
                if(HttpCode.isSucess(response.getStatus())) {
                    if(response.getData().contains(ErrorCode.OVERLAYVPN_SUCCESS)) {
                        return true;
                    }
                }
            } else {
                if(HttpCode.ERR_FAILED == response.getStatus()
                        && response.getData().contains(ErrorCode.OVERLAYVPN_PARAMETER_INVALID)) {
                    return true;
                }
            }

            return false;
        }

    }
}
