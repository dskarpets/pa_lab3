package dev.dskarpets;

import java.io.*;
import java.util.*;

public class DatabaseManager {
    private final String dataFilePath = "data.db";
    private final String indexFilePath = "index.idx";
    private int nextKey = 1;
    private PriorityQueue<Integer> freeKeys = new PriorityQueue<>();

    public DatabaseManager() throws IOException {
        File dataFile = new File(dataFilePath);
        File indexFile = new File(indexFilePath);
        if (!dataFile.exists()) dataFile.createNewFile();
        if (!indexFile.exists()) indexFile.createNewFile();

        List<Record> records = loadAllRecords();
        if (!records.isEmpty()) {
            nextKey = records.get(records.size() - 1).getKey() + 1;
        }
    }

    public int getNextKey() {
        return freeKeys.isEmpty() ? nextKey++ : freeKeys.poll();
    }

    public void addRecord(Record record) throws IOException {
        List<Record> records = loadAllRecords();
        records.add(record);
        records.sort(Comparator.comparingInt(Record::getKey));

        saveRecords(records);
        rebuildIndex(records);
    }

    public void deleteRecord(int key) throws IOException {
        List<Record> records = loadAllRecords();
        boolean removed = records.removeIf(r -> r.getKey() == key);

        if (removed) {
            freeKeys.offer(key);
            saveRecords(records);
            rebuildIndex(records);
        } else {
            throw new IOException("Record not found");
        }
    }

    private int comparisonCount = 0;

    public int getComparisonCount() {
        return comparisonCount;
    }

    public Record search(int key) throws IOException {
        comparisonCount = 0;

        List<Integer> index = loadIndex();
        int blockIndex = binarySearchIndex(index, key);

        if (blockIndex < 0) return null;

        List<Record> block = loadBlock(blockIndex);
        for (Record record : block) {
            comparisonCount++;
            if (record.getKey() == key) {
                return record;
            }
        }

        return null;
    }

    public void updateRecord(int key, String newData) throws IOException {
        List<Record> records = loadAllRecords();
        boolean updated = false;

        for (Record record : records) {
            if (record.getKey() == key) {
                record.setData(newData);
                updated = true;
                break;
            }
        }

        if (updated) {
            saveRecords(records);
            rebuildIndex(records);
        } else {
            throw new IOException("Record not found");
        }
    }

    private void saveRecords(List<Record> records) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(dataFilePath))) {
            for (Record record : records) {
                out.writeObject(record);
            }
        }
    }

    public List<Record> loadAllRecords() throws IOException {
        List<Record> records = new ArrayList<>();
        File file = new File(dataFilePath);

        if (file.length() == 0) {
            return records;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            while (true) {
                try {
                    Record record = (Record) ois.readObject();
                    records.add(record);
                } catch (EOFException e) {
                    break; // Кінець файлу
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        return records;
    }

    private void rebuildIndex(List<Record> records) throws IOException {
        List<Integer> index = new ArrayList<>();
        for (int i = 0; i < records.size(); i += 5) {
            index.add(records.get(i).getKey());
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(indexFilePath))) {
            out.writeObject(index);
        }
    }

    private List<Integer> loadIndex() throws IOException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(indexFilePath))) {
            return (List<Integer>) in.readObject();
        } catch (FileNotFoundException e) {
            return new ArrayList<>();
        } catch (ClassNotFoundException e) {
            throw new IOException("Error reading index", e);
        }
    }

    private List<Record> loadBlock(int blockIndex) throws IOException {
        List<Record> records = loadAllRecords();
        int start = blockIndex * 5;
        int end = Math.min(start + 5, records.size());
        return records.subList(start, end);
    }

    private int binarySearchIndex(List<Integer> index, int key) {
        int low = 0;
        int high = index.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            comparisonCount++;

            if (index.get(mid) == key ||
               (mid < index.size() - 1 && index.get(mid) < key && index.get(mid + 1) > key)) {
                return mid;
            } else if (index.get(mid) < key) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }
}

