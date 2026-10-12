import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;

public class Parser {
    public static String textoPdfs(String ruta) throws IOException{
        int i;
        PdfReader lector = new PdfReader(ruta);
        StringBuilder cadenatex = new StringBuilder();

        for(i = 1; i <= lector.getNumberOfPages(); i++){
            String textoPagina = PdfTextExtractor.getText(lector, i, new LocationTextExtractionStrategy());
            cadenatex.append(textoPagina).append("\n");
        }
        lector.close();
        return cadenatex.toString();
    }
    public static String textoPagina(String ruta, int pagina) throws IOException{
        PdfReader lector2 = new PdfReader(ruta);
        String texto = PdfTextExtractor.getText(lector2, pagina, new LocationTextExtractionStrategy());
        lector2.close();
        return texto;
    }
}
