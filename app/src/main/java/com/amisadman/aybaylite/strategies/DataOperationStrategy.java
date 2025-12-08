package com.amisadman.aybaylite.strategies;

import java.util.ArrayList;
import java.util.HashMap;

public interface DataOperationStrategy {
    ArrayList<HashMap<String, String>> loadData();
    boolean deleteData(String id);
    String getType();
}