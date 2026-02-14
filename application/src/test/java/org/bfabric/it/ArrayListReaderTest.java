package org.bfabric.it;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.bfabric.entity.Dataset;
import org.bfabric.exception.InvalidDataException;
import org.bfabric.util.ArrayListReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ArrayListReaderTest {

    public static final String TSV_VALID = "col1\tcol2\nval1\tval2\nval3\tval4";

    public static final String TSV_ERROR_UNMATCHED = "col1\tcol2\nval1\tval2\nval3\tval4\tval5";

    public static final String TSV_ERROR_NO_FIELDS = "col1\tcol2\n";

    public static final List<List<String>> TSV_VALID_EXPECTED = Arrays.asList(Arrays.asList("col1", "col2"), Arrays.asList("val1", "val2"), Arrays.asList("val3", "val4"));

    @Test
    public void convertsTsvFileToNestedList() throws IOException, InvalidDataException {
        Path tempFile = Files.createTempFile("test", ".tsv");
        Files.write(tempFile, TSV_VALID.getBytes(StandardCharsets.UTF_8));
        List<List<String>> result = ArrayListReader.createArrayListFromTSVFile(tempFile);
        assertEquals(TSV_VALID_EXPECTED, result);
    }

    @Test
    public void convertsTsvStringToArrayListTest() {
        List<List<String>> result = ArrayListReader.createArrayListFromTSV(TSV_VALID);
        assertEquals(TSV_VALID_EXPECTED, result);
    }

    @Test
    public void createDatasetFromTSVError1Test() {
        Dataset dataset = null;
        try {
            dataset = Dataset.createDataset(ArrayListReader.createArrayListFromTSV(TSV_ERROR_UNMATCHED));
        } catch (Exception e) {
            assertEquals("Error: fields does not match attributes! Fields [val3, val4, val5] <-> Attributes [col1, col2]", e.getMessage());
        }
        assertNull(dataset);
    }

    @Test
    public void createDatasetFromTSVError2Test() {
        Dataset dataset = null;
        try {
            dataset = Dataset.createDataset(ArrayListReader.createArrayListFromTSV(TSV_ERROR_NO_FIELDS));
        } catch (Exception e) {
            assertEquals("Error: dataset is empty, i.e., has no items!", e.getMessage());
        }
        assertNull(dataset);
    }

    @Test
    public void createDatasetFromTSVTest() throws InvalidDataException {
        Dataset dataset = Dataset.createDataset(ArrayListReader.createArrayListFromTSV(TSV_VALID));
        assertEquals(TSV_VALID_EXPECTED.get(0).size(), dataset.getAttributes().size());
        assertEquals(TSV_VALID_EXPECTED.size() - 1, dataset.getItems().size());
    }

    @Test
    public void handlesNonExistentTsvFile() {
        File nonExistentFile = new File("non_existent_file.tsv");
        InvalidDataException exception = assertThrows(InvalidDataException.class, () -> ArrayListReader.createArrayListFromTSVFile(nonExistentFile.toPath()));
        assertEquals("File Error!", exception.getMessage());
    }

    @Test
    public void tsv2DatasetTest() throws InvalidDataException {
        Dataset dataset = Dataset.createDatasetFromTSV(TSV_VALID);
        assertEquals("2", String.valueOf(dataset.getAttributes().size()));
        assertEquals("2", String.valueOf(dataset.getItems().size()));
    }
}