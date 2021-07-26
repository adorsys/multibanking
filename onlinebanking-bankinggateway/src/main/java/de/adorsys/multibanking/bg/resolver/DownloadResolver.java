package de.adorsys.multibanking.bg.resolver;

import de.adorsys.multibanking.bg.ApiClientFactory;
import de.adorsys.multibanking.domain.BalancesReport;
import de.adorsys.multibanking.domain.Booking;
import de.adorsys.multibanking.domain.response.TransactionsResponse;
import de.adorsys.multibanking.mapper.TransactionsParser;
import de.adorsys.multibanking.xs2a_adapter.ApiException;
import de.adorsys.multibanking.xs2a_adapter.ApiResponse;
import de.adorsys.multibanking.xs2a_adapter.api.DownloadControllerApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public class DownloadResolver {
    private final DownloadControllerApi downloadControllerApi;
    private final boolean dumpDownloadFiles;

    public DownloadResolver(String xs2aAdapterBaseUrl, boolean dumpDownloadFiles) {
        this.downloadControllerApi = ApiClientFactory.xs2aAdapterDownloadControllerApi(xs2aAdapterBaseUrl);
        this.dumpDownloadFiles = dumpDownloadFiles;
    }

    public TransactionsResponse loadTransactions(String downloadlink, String bankCode, String consentId) throws ApiException, IOException {
        log.info("Trying to downwload: {}", downloadlink);

        ApiResponse<byte[]> apiResponse = downloadControllerApi.downloadWithHttpInfo(downloadlink, UUID.randomUUID(), bankCode, consentId);

        // there is no way to determine which content-type is downloaded since xs2a-adapter replaces any content-type with application/octet-stream
        // content-disposition is filtered by the xs2a-adapter so we must assume that the download is a zip with camts

        log.info("Assume that download is a zip with camt files");

        byte[] zip = apiResponse.getData();

        if (dumpDownloadFiles) {
            Files.write(Files.createFile(Paths.get("/tmp/download_" + System.currentTimeMillis() + ".zip")), zip);
        }

        return readZip(zip);
    }


    TransactionsResponse readZip(byte[] zip) throws IOException{
        List<String> rawData = new ArrayList<>();
        List<Booking> bookings = new ArrayList<>();
        BalancesReport balancesReport = null;

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zip)) {
            try (ZipInputStream zipInputStream = new ZipInputStream(byteArrayInputStream, StandardCharsets.ISO_8859_1)) {
                ZipEntry zipEntry = zipInputStream.getNextEntry();

                if (zipEntry == null) {
                    log.error("Download is not a ZIP");
                    return TransactionsResponse.builder().bookings(Collections.emptyList()).build();
                }

                do {
                    String filename = zipEntry.getName();
                    if(!Optional.ofNullable(filename).map(n -> n.toLowerCase().endsWith("xml")).orElse(false)) {
                        log.error("Unexpected zip Entry: {}", filename);
                    } else {
                        log.info("Unzip entry: {}", filename);
                        String camt = IOUtils.toString(zipInputStream, StandardCharsets.UTF_8);
                        TransactionsResponse temp = TransactionsParser.camtStringToLoadBookingsResponse(camt);
                        rawData.add(camt);
                        bookings.addAll(temp.getBookings());
                        balancesReport = temp.getBalancesReport();
                    }
                } while ((zipEntry = zipInputStream.getNextEntry()) != null);
            }
        }
        return TransactionsResponse.builder()
            .rawData(rawData)
            .bookings(bookings)
            .balancesReport(balancesReport)
            .build();
    }
}
