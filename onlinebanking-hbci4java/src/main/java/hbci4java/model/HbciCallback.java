/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hbci4java.model;

import exception.InvalidPinException;
import lombok.extern.slf4j.Slf4j;
import org.kapott.hbci.callback.HBCICallback;

/**
 * Created by alexg on 08.02.17.
 */
@Slf4j
public class HbciCallback implements HBCICallback {

    @Override
    public void callback(int reason, String msg, int datatype, StringBuffer retData) {
        switch (reason) {
            case HBCICallback.NEED_PT_PHOTOTAN: {
                String hhduc = retData.toString();
                break;
            }
            case HBCICallback.WRONG_PIN: {
                throw new InvalidPinException();
            }
            // No need to tell when we may open or close our internet connection
            case HBCICallback.NEED_CONNECTION:
            case HBCICallback.CLOSE_CONNECTION:
                log.debug("Callback: reason: {}, message: {}", reason, msg);
                break;
            default:
                log.warn("Callback: reason: {}, message: {}", reason, msg);
        }
    }

    @Override
    public void tanChallengeCallback(String orderRef, String challenge) {
    }

    @Override
    public String needTAN() {
        return null;
    }

    @Override
    public void status(int statusTag, Object[] o) {
        log.debug("Status: {} {}, objects: {}", statusTag, statusToString(statusTag), o);
    }

    @Override
    public void status(int statusTag, Object o) {
        status(statusTag, new Object[]{o});
    }

    String statusToString(int status) {
        switch (status) {
            case STATUS_SEND_TASK:
                return "STATUS_SEND_TASK";
            case STATUS_SEND_TASK_DONE:
                return "STATUS_SEND_TASK_DONE";
            case STATUS_INST_BPD_INIT:
                return "STATUS_INST_BPD_INIT";
            case STATUS_INST_BPD_INIT_DONE:
                return "STATUS_INST_BPD_INIT_DONE";
            case STATUS_INST_GET_KEYS:
                return "STATUS_INST_GET_KEYS";
            case STATUS_INST_GET_KEYS_DONE:
                return "STATUS_INST_GET_KEYS_DONE";
            case STATUS_SEND_KEYS:
                return "STATUS_SEND_KEYS";
            case STATUS_SEND_KEYS_DONE:
                return "STATUS_SEND_KEYS_DONE";
            case STATUS_INIT_SYSID:
                return "STATUS_INIT_SYSID";
            case STATUS_INIT_SYSID_DONE:
                return "STATUS_INIT_SYSID_DONE";
            case STATUS_INIT_UPD:
                return "STATUS_INIT_UPD";
            case STATUS_INIT_UPD_DONE:
                return "STATUS_INIT_UPD_DONE";
            case STATUS_LOCK_KEYS:
                return "STATUS_LOCK_KEYS";
            case STATUS_LOCK_KEYS_DONE:
                return "STATUS_LOCK_KEYS_DONE";
            case STATUS_INIT_SIGID:
                return "STATUS_INIT_SIGID";
            case STATUS_INIT_SIGID_DONE:
                return "STATUS_INIT_SIGID_DONE";
            case STATUS_DIALOG_INIT:
                return "STATUS_DIALOG_INIT";
            case STATUS_DIALOG_INIT_DONE:
                return "STATUS_DIALOG_INIT_DONE";
            case STATUS_DIALOG_END:
                return "STATUS_DIALOG_END";
            case STATUS_DIALOG_END_DONE:
                return "STATUS_DIALOG_END_DONE";
            case STATUS_MSG_CREATE:
                return "STATUS_MSG_CREATE";
            case STATUS_MSG_SIGN:
                return "STATUS_MSG_SIGN";
            case STATUS_MSG_CRYPT:
                return "STATUS_MSG_CRYPT";
            case STATUS_MSG_SEND:
                return "STATUS_MSG_SEND";
            case STATUS_MSG_DECRYPT:
                return "STATUS_MSG_DECRYPT";
            case STATUS_MSG_VERIFY:
                return "STATUS_MSG_VERIFY";
            case STATUS_MSG_RECV:
                return "STATUS_MSG_RECV";
            case STATUS_MSG_PARSE:
                return "STATUS_MSG_PARSE";
            case STATUS_SEND_INFOPOINT_DATA:
                return "STATUS_SEND_INFOPOINT_DATA";
            case STATUS_MSG_RAW_SEND:
                return "STATUS_MSG_RAW_SEND";
            case STATUS_MSG_RAW_RECV:
                return "STATUS_MSG_RAW_RECV";
            default:
                return "?";
        }
    }
}
