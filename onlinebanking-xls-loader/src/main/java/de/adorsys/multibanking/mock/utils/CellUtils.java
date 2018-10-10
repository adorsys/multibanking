package de.adorsys.multibanking.mock.utils;

import de.adorsys.multibanking.mock.exception.InvalidRowException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CellUtils {
    public static String stringCell(Row row, int index, boolean optional) {
        Cell cell = row.getCell(index);
        if (cell != null && StringUtils.isNotBlank(cell.getStringCellValue())) {
            return cell.getStringCellValue().trim();
        }
        return nullOrException(index, optional);
    }

    public static BigDecimal bigDecimalCell(Row row, int index, boolean optional) {
        Cell cell = row.getCell(index);
        if (cell != null) {
            try {
                try {
                    cell.setCellType(CellType.NUMERIC);
                    return new BigDecimal(cell.getNumericCellValue());
                } catch (Exception e) {
                    return new BigDecimal(cell.getStringCellValue());
                }
            } catch (Exception e) {
                invalideException(LocalDate.class, index, optional, e);
            }
        }
        return nullOrException(index, optional);
    }

    public static LocalDate localDate(Row row, int index, boolean optional) {
        Cell cell = row.getCell(index);
        try {
            return LocalDate.parse(cell.getStringCellValue());
        } catch (Exception e) {
            invalideException(LocalDate.class, index, optional, e);
        }
        return nullOrException(index, optional);
    }

    public static Boolean booleanCell(Row row, int index, boolean optional) {
        Cell cell = row.getCell(index);
        if (cell != null) {
            return cell.getBooleanCellValue();
        }
        return nullOrException(index, optional);
    }

    public static String stringFromNumCell(Row row, int index, boolean optional) {
        Cell cell = row.getCell(index);
        if (cell != null) {
            try {
                return cell.getStringCellValue();
            } catch (Exception e) {
                return String.format("%s", Double.valueOf(cell.getNumericCellValue()).intValue());
            }
        }
        return nullOrException(index, optional);
    }


    private static <T> T nullOrException(int index, boolean optional) {
        if (optional) return null;
        throw new InvalidRowException(String.format("Row at %s null or empty: ", index));
    }

    private static <T> void invalideException(Class<T> klass, int index, boolean optional, Exception ex) {
        if (!optional)
            throw new InvalidRowException(String.format("Row at %s can not be converted to type %s: ", index, klass.getName()), ex);
    }
}
