package com.eeseetech.nagrand.entity;

import java.util.List;

public class MessageResponseData extends ResponseData{

    public DataBean data;

    public static class DataBean {
        /**
         * retcode : 1
         * tag : 1493175200
         * msgList : [{"indate":{"startday":"2017-03-28","endday":"2017-04-29"},"timelist":[{"start":"00:40","end":"01:40"},{"start":"08:00","end":"11:00"}],"position":"1","msg":""},{"indate":{"startday":"2017-03-28","endday":"2017-04-29"},"timelist":[{"start":"00:40","end":"01:40"},{"start":"08:00","end":"11:00"}],"position":"1","msg":""}]
         */

        public int retcode;
        public String tag;
        public List<MessageInfo> msgList;
    }
}
