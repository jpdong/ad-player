package com.eeseetech.nagrand.api;

import com.eeseetech.nagrand.entity.VideoInfo;
import com.eeseetech.nagrand.entity.Result;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.eeseetech.nagrand.entity.Result.NO_CHANGE;
import static com.eeseetech.nagrand.entity.Result.SUCCESS;
import static org.junit.Assert.*;

public class ProviderTest {

    @Test
    public void videos() {
        Provider provider = new Provider();
        Result<Map<String,VideoInfo>> result = provider.videos("0", "000000000000", "000000000000");
        System.out.println("result data size = " + result.data.size());
        String tag = result.tag;
        assertTrue(result.code == SUCCESS);
        assertTrue(result.data.size() > 0);
        Result<Map<String,VideoInfo>> result1 = provider.videos(tag, "000000000000", "000000000000");
        assertTrue(result1.code == NO_CHANGE);
        Result<Map<String,VideoInfo>> result2 = provider.videos("0", "00000000000", "000000000000");
        //assertTrue(result2.code == FAIL);
        System.out.println("error : " + result2.code);
        System.out.println("result data size = " + result2.data.size());
        System.out.println("error : " + result2.msg);
    }

    public void println(String s) {
        System.out.println(s);
    }

    @Test
    public void playList() {
        Provider provider = new Provider();
        Result<List<String>> result = provider.playList("0", "000000000000", "000000000000");
        System.out.println("result data size = " + result.data.size());
        String tag = result.tag;
        assertTrue(result.code == SUCCESS);
        assertTrue(result.data.size() > 0);
        Result<List<String>> result1 = provider.playList(tag, "000000000000", "000000000000");
        assertTrue(result1.code == NO_CHANGE);
        Result<List<String>> result2 = provider.playList("0", "00000000000", "000000000000");
        //assertTrue(result2.code == FAIL);
        System.out.println("error : " + result2.code);
        System.out.println("result data size = " + result2.data.size());
        System.out.println("error : " + result2.msg);
    }

    @Test
    public void addPlayHistory() {
        Provider provider = new Provider();
        Result<Boolean> result = provider.addPlayHistory(1, "000000000000", "000000000000","test.mp4");
        assertTrue(result.code == SUCCESS);
    }
}