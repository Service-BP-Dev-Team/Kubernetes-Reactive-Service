package com.reactive.service.app.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import com.consulner.app.api.mergesort.ToolKit;

public class RLECompression {
    public static String compress(String input) {
        StringBuilder compressed = new StringBuilder();

        int count = 1;
        for (int i = 1; i < input.length(); i++) {
            if (input.charAt(i) == input.charAt(i - 1)) {
                count++;
            } else {
                compressed.append(input.charAt(i - 1));
                compressed.append(count);
                count = 1;
            }
        }

        // Append the last character and its count
        compressed.append(input.charAt(input.length() - 1));
        compressed.append(count);

        // Check if the compressed string is shorter than the original string
        if (compressed.length() < input.length()) {
            return compressed.toString();
        } else {
            return input;
        }
    }

    public static String uncompress(String input) {
        StringBuilder uncompressed = new StringBuilder();

        for (int i = 0; i < input.length(); i += 2) {
            char character = input.charAt(i);
            int count = Character.getNumericValue(input.charAt(i + 1));

            for (int j = 0; j < count; j++) {
                uncompressed.append(character);
            }
        }

        return uncompressed.toString();
    }

    public static void main(String[] args) {
    	 try {
         	ArrayList<Integer> inputTable = ToolKit.generateArray(10000);
             String original = inputTable.toString() ;
             long start = System.currentTimeMillis();
             byte[] compressedData = compress(original).getBytes();
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
         } catch (Exception e) {
             e.printStackTrace();
         }
    }
}