package com.common.library.net.bean;

/**
 * @author:xuruibin
 * @date:2020/9/1 Description:
 */
public class Qrcode {

    /**
     * code : 1
     * msg : 数据返回成功
     * data : {"qrCodeUrl":"http://www.mxnzp.com/api_file/qrcode/7/2/d/d/0/9/a/e/327588b1ddb44cf7a95e43d7ad2f5b90.png","content":"你好","type":0,"qrCodeBase64":null}
     */

    private int code;
    private String msg;
    private DataBean data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * qrCodeUrl : http://www.mxnzp.com/api_file/qrcode/7/2/d/d/0/9/a/e/327588b1ddb44cf7a95e43d7ad2f5b90.png
         * content : 你好
         * type : 0
         * qrCodeBase64 : null
         */

        private String qrCodeUrl;
        private String content;
        private int type;
        private Object qrCodeBase64;

        public String getQrCodeUrl() {
            return qrCodeUrl;
        }

        public void setQrCodeUrl(String qrCodeUrl) {
            this.qrCodeUrl = qrCodeUrl;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public Object getQrCodeBase64() {
            return qrCodeBase64;
        }

        public void setQrCodeBase64(Object qrCodeBase64) {
            this.qrCodeBase64 = qrCodeBase64;
        }
    }
}
