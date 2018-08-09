package com.eeseetech.nagrand.entity;

public class HistoryRequestData {

    public long delay;
    public String router;
    public String self;
    public String file;

    public HistoryRequestData(long delay, String router, String self, String file) {
        this.delay = delay;
        this.router = router;
        this.self = self;
        this.file = file;
    }
}
