import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main {

    public static void main(String[] args) throws Exception {
        generate("123446532634", 0, 5, 5);
    }

    private static String generateZipName(String productId, int productOffset, int numberOfItems, int packageSize) {
        final int end = productOffset + (numberOfItems * packageSize);
        return productId + "-" + productOffset + "-" + end + ".zip";
    }

    private static JSONObject buildJSONObject(String productId, int productOffset) {
        JSONObject item = new JSONObject();
        item.put("productId", productId);
        item.put("offset", productOffset);
        return item;
    }

    private static void putZipOutputEntry(ZipOutputStream zipOut, String entryName, String barcodeText) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(generate(barcodeText), "jpeg", os);
        InputStream fis = new ByteArrayInputStream(os.toByteArray());
        ZipEntry zipEntry = new ZipEntry(entryName + ".jpeg");
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    private static void generate(String productId, int productOffset, int numberOfItems, int packageSize) throws Exception {
        FileOutputStream fos = new FileOutputStream(generateZipName(productId, productOffset, numberOfItems, packageSize));
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        for (int i = 0; i < packageSize; i++) {
            JSONArray array = new JSONArray();
            int startProductOffset = productOffset;
            for (int j = 0; j < numberOfItems; j++) {
                JSONObject item = buildJSONObject(productId, productOffset);
                array.put(buildJSONObject(productId, productOffset));
                putZipOutputEntry(zipOut, productId + "-" + productOffset, item.toString());
                productOffset++;
            }
            if (packageSize > 1){
                putZipOutputEntry(zipOut, productId + "-" + startProductOffset + "-" + productOffset, array.toString());
            }
        }
        zipOut.close();
        fos.close();
    }

    private static BufferedImage generate(String barcodeText) throws Exception {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, 200, 200);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
}
