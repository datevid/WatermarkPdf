/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package watermarkpdf;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.*;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;


public class WatermarkPdf {
    private byte[] pdfBytesOriginal;

    public WatermarkPdf(byte[] pdfBytesOriginal) {
        this.pdfBytesOriginal = pdfBytesOriginal;
    }

    public WatermarkPdf() {
        throw new RuntimeException("Error. Por favor envie el documento PDF original al crear el constructor");
    }

    /**
     * agrega marca de agua al documento original
     * El documento original se debe enviarse en el constructor
     * Retorna el documento con marca de agua,
     * @return Retorna un array de bytes del nuevo documento, con la marca de agua agregado
     * more: https://goo.gl/B6Q3X3
     * more: https://goo.gl/5yV48V , https://goo.gl/cSYuPp , 
     * m
     * @throws DocumentException
     * @throws IOException
     * @throws Exception
     */
    public byte[] addWatermark() throws DocumentException, IOException, Exception{
        Document doc = new Document();
        ByteArrayOutputStream pdfWatermark = new ByteArrayOutputStream();

        PdfWriter writer = PdfWriter.getInstance(doc, pdfWatermark);
        doc.open();
        PdfContentByte cb = writer.getDirectContent();

        PdfReader reader = new PdfReader(pdfBytesOriginal);
        int nPages = reader.getNumberOfPages();

        //get image
        //Image image=Image.getInstance(filePathQR);


        for (int i = 1; i <= nPages; i++) {
            PdfImportedPage page = writer.getImportedPage(reader, i);
            doc.newPage();
            cb.addTemplate(page, 0, 0);

            //marca de agua en lineas de 45 grados.
            String baseText = "WatermarkPDF " + getCurrentTime() + " ";
            baseText = String.join("", Collections.nCopies(5, baseText));

            float h = page.getHeight();
            float w = page.getWidth();
            Phrase phrase3 = showText(baseText, 15);
            PdfGState gs1 = new PdfGState();

            cb.saveState();
            gs1.setFillOpacity(0.4f);
            cb.setGState(gs1);
            for (int j = 1; j <= 10; j++) {
                ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, phrase3, 0, h - (j) * 150, 45);
            }
            cb.restoreState();


            //QR
            //image.setAbsolutePosition(55,5);
            //doc.add(image);


            //Pie de Pagina.
            int x = 90;
            int y = 30;
            int spaceLine = 7;
            Phrase phrase1 = showText("Comité de Gerencia Acta Nº 000001-2015-CG Ex. GG0000201700000005", 8);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, phrase1, x, y, 0);

            Phrase phrase2 = showText("Impreso por: <Nombre de usuario aqui>. Pagina " + i + " de " + nPages, 8);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, phrase2, x, y - spaceLine, 0);

        }
        doc.close();
        byte[] pdfWatermarkBytes = pdfWatermark.toByteArray();
        return pdfWatermarkBytes;
    }

    /**
     * Retorna un documento pdf de ejemplo
     * @return retorna en array de bytes
     * @throws DocumentException
     */
    public static byte[] pdfExample() throws DocumentException{
        // step 1
        Document document = new Document();
        // step 2
        // we'll create the file in memory
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        // step 3
        document.open();
        // step 4
        document.add(new Paragraph("pdfCualquiera!"));
        // step 5
        document.close();
        return baos.toByteArray();
    }

    /**
     * guarda un archivo pdf
     * @param inputPdf indicar el archivo pdf en bytes
     * @param fileName indicar el nombre del archivo
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void save(byte[] inputPdf, String fileName) throws FileNotFoundException, IOException{
        // let's write the file in memory to a file anyway
        FileOutputStream fos = new FileOutputStream(fileName);//"abc.pdf"
        fos.write(inputPdf);
        fos.close();
    }

    public static void main(String[] args){
        try {

            WatermarkPdf watermark = new WatermarkPdf(WatermarkPdf.pdfExample());
            byte[] pdfWatermark = watermark.addWatermark();

            // show in console log
            ByteArrayOutputStream baos = new ByteArrayOutputStream(pdfWatermark.length);
            baos.write(pdfWatermark, 0, pdfWatermark.length);
            System.out.println("pdf:");
            System.out.println(baos);
            
            WatermarkPdf.save(pdfWatermark, "abc.pdf");
            
            System.out.println("Finish!");

        } catch (Exception ex) {
            System.out.println("Error al realizar el test. "+ ex.getMessage());
        }
    }
    public static Phrase showText(String text, int fontSize) throws Exception {
        BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, false);
        Font fontNormal = new Font(baseFont, fontSize, Font.NORMAL, Color.black);
        return new Phrase(text, fontNormal);
    }

    public static String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now); //2016/11/16 12:08:43
    }
}
