package com.fowlart.main.hi_level_api_usage;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.nio.charset.StandardCharsets;

public class BloomFilterStandardLibExample {

    public static void main(String[] args) {

        // Create a Bloom Filter instance
        BloomFilter<String> names = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 10000);

        // Add the data sets
        names.put("Olena");
        names.put("Melaniya");
        names.put("Artur");

        // Test the bloom filter
        System.out.println(names.mightContain("Artur"));
        System.out.println(names.mightContain("Zenoviy"));

    }
}
