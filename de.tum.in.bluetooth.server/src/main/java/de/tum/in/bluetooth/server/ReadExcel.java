/*******************************************************************************
 * Copyright 2015 Amit Kumar Mondal <admin@amitinside.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package de.tum.in.bluetooth.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.common.collect.Lists;

public class ReadExcel {

	private static void getAndPrintData(final XSSFSheet sheet) {

		System.out.println(" -- getting data from excel -------");
		final Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			final Row row = rowIterator.next();
			final Iterator<Cell> cellIterator = row.cellIterator();
			while (cellIterator.hasNext()) {
				final Cell cell = cellIterator.next();

				switch (cell.getCellType()) {
				case Cell.CELL_TYPE_NUMERIC:
					System.out.print(cell.getNumericCellValue() + "\t");
					break;
				case Cell.CELL_TYPE_STRING:
					System.out.print(cell.getStringCellValue() + "\t");
					break;

				}
			}
			System.out.println();
		}

	}

	private static String[] getColumnNames(final Row column) {
		final String columns[] = new String[column.getPhysicalNumberOfCells()];
		final Iterator<Cell> cellIterator = column.cellIterator();
		int i = 0;
		while (cellIterator.hasNext()) {
			final Cell cell = cellIterator.next();
			columns[i++] = cell.getStringCellValue();
		}

		return columns;
	}

	private static Object getTypeValue(final Class<?> type, final Cell cell) {
		Object typedValue = null;
		if (type == int.class) {
			typedValue = (int) cell.getNumericCellValue();
		} else if (type == double.class) {
			typedValue = cell.getNumericCellValue();
		} else if (type == boolean.class) {
			typedValue = cell.getBooleanCellValue();
		} else if (type == String.class) {
			typedValue = cell.getStringCellValue();
		}
		return typedValue;
	}

	private static List<BluetoothData> loadDataToList(final XSSFSheet sheet, final List<BluetoothData> data,
			final BluetoothData bluetoothData)
					throws InstantiationException, IllegalAccessException, SecurityException, NoSuchFieldException {

		final Row column = sheet.getRow(0);
		final String columnNames[] = getColumnNames(column);

		final Iterator<Row> rowIterator = sheet.iterator();

		int rowOne = 0;

		while (rowIterator.hasNext()) {
			final Row row = rowIterator.next();
			final Iterator<Cell> cellIterator = row.cellIterator();

			final BluetoothData newRecord = bluetoothData.getClass().newInstance();

			// Skip First row which is column names
			if (rowOne > 0) {
				int i = 0;
				while (cellIterator.hasNext()) {
					final Cell cell = cellIterator.next();
					final String columnName = columnNames[i++];
					final Field f1 = bluetoothData.getClass().getDeclaredField(columnName.trim());
					f1.setAccessible(true);
					f1.set(newRecord, getTypeValue(f1.getType(), cell));

				}
				data.add(newRecord);
			}
			rowOne++;
		}
		return data;

	}

	public static List<BluetoothData> read() throws IOException {

		final List<BluetoothData> data = Lists.newArrayList();
		XSSFWorkbook workbook = null;

		try {

			final File file = new File("/home/pi/TUM/GS_d8n19000fz01ap06.xlsx");

			final FileInputStream fileStream = new FileInputStream(file);
			// Get the workbook instance for XLS file
			workbook = new XSSFWorkbook(fileStream);
			// Get first sheet from the workbook
			final XSSFSheet sheet = workbook.getSheetAt(0);

			getAndPrintData(sheet);

			// load data from excel file

			final BluetoothData bluetoothData = new BluetoothData();
			loadDataToList(sheet, data, bluetoothData);

			for (final BluetoothData dat : data) {
				System.out.print(dat.getTime() + "\t");
				System.out.print(dat.getForce_x() + "\t");
				System.out.print(dat.getForce_y() + "\t");
				System.out.print(dat.getForce_z() + "\t");
				System.out.print(dat.getTorqueX() + "\t");
				System.out.print(dat.getTorqueY() + "\t");
				System.out.print(dat.getTorqueZ() + "\t");
				System.out.print(dat.getSpindleSpeed() + "\t");
				System.out.print(dat.getDepthCut() + "\t");
				System.out.print(dat.getFeed());
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			System.out.println(e);
		} catch (final SecurityException e) {
			e.printStackTrace();
		} catch (final InstantiationException e) {
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
		} catch (final NoSuchFieldException e) {
			e.printStackTrace();
		} finally {
			workbook.close();
		}
		return data;
	}
}