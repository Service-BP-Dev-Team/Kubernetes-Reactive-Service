package com.reactive.service.app.api;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.consulner.app.api.mergesort.RandomInputRequest;
import com.consulner.app.api.mergesort.ToolKit;

public class GzipCompressor {
	
	
    public static byte[] compress(String input) throws IOException {
    	if (input.length()>InMemoryWorkspace.getNetworkDataCompressionThreshold()) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(input.getBytes());
        }
        return byteArrayOutputStream.toByteArray();
    	}
    	return input.getBytes();
    }

    public static boolean isCompressed(byte[] data) {
        return data.length >= 2 && data[0] == (byte) GZIPInputStream.GZIP_MAGIC
                && data[1] == (byte) (GZIPInputStream.GZIP_MAGIC >>> 8);
    }

    public static String decompress(byte[] compressedData) throws IOException {
    	if (!isCompressed(compressedData)) {
            return new String(compressedData);
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressedData);
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = gzipInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toString();
        }
    }

    public static void main(String[] args) {
        try {
        	ArrayList<Integer> inputTable = ToolKit.generateArray(1000000);
            String original = inputTable.toString() ;
            long start = System.currentTimeMillis();
            byte[] compressedData = compress(original);
            long end = System.currentTimeMillis();
            System.out.println("original data: "+original.getBytes().length);
            System.out.println("Compressed data: " + compressedData.length);
            System.out.println("Compression time: " + (end-start));
            
            long startsort = System.currentTimeMillis();
            Collections.sort(inputTable);
            long endsort = System.currentTimeMillis();
            System.out.println("sort time: " + (endsort-startsort));

            //String decompressed = decompress(compressedData);
            //System.out.println("Decompressed: " + decompressed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}