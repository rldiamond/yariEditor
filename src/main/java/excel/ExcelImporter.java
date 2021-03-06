/*
 * This file is part of Yari Editor.
 *
 *  Yari Editor is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Yari Editor is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with Yari Editor. If not, see <http://www.gnu.org/licenses/>.
 */

package excel;

import objects.ComparatorType;
import objects.DataType;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yari.core.table.DecisionTable;
import org.yari.core.table.TableAction;
import org.yari.core.table.TableCondition;
import org.yari.core.table.TableRow;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows importing of Excel files to DecisionTable objects.
 */
public class ExcelImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelImporter.class);
    private List<String> errorMessages = new ArrayList<>();

    /**
     * Attempt to import the provided file as an Excel workbook to a DecisionTable object.
     * Note: This not performed asynchronously, but can be.
     *
     * @param file the File to import from.
     * @return a populated (but possibly invalid) DecisionTable.
     * @throws ExcelImportException may be thrown from Apache POI when creating the workbook.
     */
    public DecisionTable importFromExcel(File file) throws ExcelImportException {
        //create Apache POI workbook from .xls/.xlsx file
        Workbook workbook;

        try {
            workbook = WorkbookFactory.create(file);
        } catch (IOException | InvalidFormatException ex) {
            throw new ExcelImportException(ex.getMessage());
        }

        if (workbook == null) {
            throw new ExcelImportException("An error occurred when importing the Excel file. The workbook was null.");
        }

        //NOTE: expect only one sheet, we only process first
        Sheet sheet = workbook.getSheetAt(0);

        if (sheet == null) {
            throw new ExcelImportException("An error occurred when importing the Excel file. The sheet was null.");
        }

        DecisionTable decisionTable = new DecisionTable();
        List<ExcelDecisionTableItem> excelDecisionTableItemList = new ArrayList<>();

        for (Row row : sheet) {
            int rowNum = row.getRowNum();
            switch (rowNum) {
                case 0:
                    processTypeRow(row, excelDecisionTableItemList);
                    break;
                case 1:
                    processDataTypeRow(row, excelDecisionTableItemList);
                    break;
                case 2:
                    processComparatorTypeRow(row, excelDecisionTableItemList);
                    break;
                case 3:
                    processMethodNameRow(row, excelDecisionTableItemList);
                    break;
                case 4:
                    processNameRow(row, excelDecisionTableItemList);
                    populateTable(excelDecisionTableItemList, decisionTable);
                    break;
                default:
                    processDataRow(row, excelDecisionTableItemList, decisionTable);
                    break;
            }

        }

        return decisionTable;
    }

    private void processDataRow(Row row, List<ExcelDecisionTableItem> excelDecisionTableItemList, DecisionTable decisionTable) {
        DataFormatter dataFormatter = new DataFormatter();
        TableRow tableRow = new TableRow();

        row.forEach(cell -> {

            if (cell.getColumnIndex() == 0) {
                return;//this is empty
            }

            String cellValue = dataFormatter.formatCellValue(cell).trim();

            if ("".equals(cellValue)) {
                return;
            }

            int i = cell.getColumnIndex() - 1;
            ExcelDecisionTableItem excelDecisionTableItem = excelDecisionTableItemList.get(i);
            switch (excelDecisionTableItem.getType()) {
                case CONDITION:
                    tableRow.getValues().add(cellValue);
                    break;
                case ACTION:
                    tableRow.getResults().add(cellValue);
                    break;
            }

        });
        decisionTable.getRawRowData().add(tableRow);

    }

    private void populateTable(List<ExcelDecisionTableItem> excelDecisionTableItemList, DecisionTable decisionTable) {
        excelDecisionTableItemList.forEach(item -> {

            switch (item.getType()) {
                case CONDITION:
                    TableCondition tableCondition = new TableCondition();
                    tableCondition.setComparator(item.getComparatorType().getTableValue());
                    tableCondition.setDataType(item.getDataType().getDisplayValue());
                    tableCondition.setMethodName(item.getMethodName());
                    tableCondition.setName(item.getName());
                    decisionTable.getTableConditions().add(tableCondition);
                    break;
                case ACTION:
                    TableAction tableAction = new TableAction();
                    tableAction.setDatatype(item.getDataType().getDisplayValue());
                    tableAction.setMethodname(item.getMethodName());
                    tableAction.setName(item.getName());
                    decisionTable.getTableActions().add(tableAction);
                    break;
            }

        });
    }

    private void processMethodNameRow(Row row, List<ExcelDecisionTableItem> excelDecisionTableItemList) {
        DataFormatter dataFormatter = new DataFormatter();

        row.forEach(cell -> {

            String cellValue = dataFormatter.formatCellValue(cell).trim();

            int columnIndex = cell.getColumnIndex();

            if (columnIndex == 0) {
                return;
            }

            int i = columnIndex - 1;

            excelDecisionTableItemList.get(i).setMethodName(cellValue);

        });
    }

    private void processNameRow(Row row, List<ExcelDecisionTableItem> excelDecisionTableItemList) {
        DataFormatter dataFormatter = new DataFormatter();

        row.forEach(cell -> {

            String cellValue = dataFormatter.formatCellValue(cell).trim();

            int columnIndex = cell.getColumnIndex();

            if (columnIndex == 0) {
                return;
            }

            int i = columnIndex - 1;

            excelDecisionTableItemList.get(i).setName(cellValue);

        });
    }

    private void processTypeRow(Row row, List<ExcelDecisionTableItem> excelDecisionTableItemList) throws ExcelImportException {
        DataFormatter dataFormatter = new DataFormatter();

        for (Cell cell : row) {
            String cellValue = dataFormatter.formatCellValue(cell).trim().toUpperCase();

            ExcelDecisionTableItem excelTableItem = new ExcelDecisionTableItem();
            switch (cellValue) {
                case "TYPE":
                    break;
                case "CONDITION":
                    excelTableItem.setType(ExcelDecisionTableItem.Type.CONDITION);
                    excelDecisionTableItemList.add(excelTableItem);
                    break;
                case "ACTION":
                    excelTableItem.setType(ExcelDecisionTableItem.Type.ACTION);
                    excelDecisionTableItemList.add(excelTableItem);
                    break;
                default:
                    String errorMessage = "Invalid Data Type of " + cellValue + ". Cannot proceed.";
                    errorMessages.add(errorMessage);
                    throw new ExcelImportException(errorMessage);
            }

        }
    }

    private void processDataTypeRow(Row row, List<ExcelDecisionTableItem> excelDecisionTableItemList) {
        DataFormatter dataFormatter = new DataFormatter();

        row.forEach(cell -> {

            String cellValue = dataFormatter.formatCellValue(cell).trim().toUpperCase();

            int columnIndex = cell.getColumnIndex();

            if (columnIndex == 0) {
                return;
            }

            int i = columnIndex - 1;

            DataType dataType = DataType.getFromTableString(cellValue);

            if (dataType != null) {
                excelDecisionTableItemList.get(i).setDataType(dataType);
            } else {
                errorMessages.add("Data Type " + cellValue + " entered in column " + columnIndex + " is invalid. Auto-set to String.");
                excelDecisionTableItemList.get(i).setDataType(DataType.STRING);
            }

        });
    }

    private void processComparatorTypeRow(Row row, List<ExcelDecisionTableItem> excelDecisionTableItemList) {
        DataFormatter dataFormatter = new DataFormatter();

        row.forEach(cell -> {

            String cellValue = dataFormatter.formatCellValue(cell).trim().toUpperCase();

            if ("COMPARATOR TYPE".equalsIgnoreCase(cellValue)) {
                return;
            }

            ComparatorType comparatorType = ComparatorType.getFromTableString(cellValue);

            int i = cell.getColumnIndex() - 1;

            if (comparatorType != null) {
                excelDecisionTableItemList.get(i).setComparatorType(comparatorType);
            } else if (!excelDecisionTableItemList.get(i).getType().equals(ExcelDecisionTableItem.Type.ACTION)) {
                errorMessages.add("Comparator Type " + cellValue + " entered in column " + cell.getColumnIndex() + " is invalid. Auto-set to ==.");
                excelDecisionTableItemList.get(i).setComparatorType(ComparatorType.EQUAL);
            }

        });
    }

    /**
     * Return a list of error messages.
     *
     * @return a list of error messages.
     */
    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public class ExcelImportException extends Exception {

        public ExcelImportException() {
            super();
        }

        public ExcelImportException(String message) {
            super(message);
        }

        public ExcelImportException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
