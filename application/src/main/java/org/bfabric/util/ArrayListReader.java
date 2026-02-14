/*
    MIT License

    Copyright (c) 2005-2026 Functional Genomics Center Zurich, UZH/ETH Zurich

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
 */

package org.bfabric.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.bfabric.exception.InvalidDataException;

@Named
public class ArrayListReader {

    public static List<List<String>> createArrayList(String value, String delimiter) {
        List<List<String>> records = new ArrayList<>();
        Arrays.stream(value.split("\n")).forEach(line -> records.add(Arrays.asList(line.split(delimiter))));
        return records;
    }

    public static List<List<String>> createArrayListFromCSV(String value) {
        return createArrayList(value, ",");
    }

    public static List<List<String>> createArrayListFromCSVFile(Path path) throws InvalidDataException {
        return createArrayListFromFile(path, ",");
    }

    public static List<List<String>> createArrayListFromFile(Path path, String delimiter) throws InvalidDataException {
        try {
            return Files.lines(path).map(line -> Arrays.asList(line.split(delimiter))).collect(Collectors.toList());
        } catch (IOException e) {
            throw new InvalidDataException("File Error!");
        }
    }

    public static List<List<String>> createArrayListFromTSV(String value) {
        return createArrayList(value, "\t");
    }

    public static List<List<String>> createArrayListFromTSVFile(Path path) throws InvalidDataException {
        return createArrayListFromFile(path, "\t");
    }
}