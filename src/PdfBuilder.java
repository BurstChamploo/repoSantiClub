/**
 *
 * @author Evelin
 */
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

public class PdfBuilder {

    /*Guarda el ResultSet que llega ya armado desde fuera (con los datos
    de la prematricula: cedula, nombre del estudiante y sus materias)*/
    private final ResultSet prematricula;

    /*Constructor: PdfBuilder no consulta la base de datos, solo recibe
    el resultado ya ejecutado por otra clase (CrudManager/main)*/
    public PdfBuilder(ResultSet prematricula) {
        this.prematricula = prematricula;
    }

    public void crearPdf() {
        //Crea el documento en tamaño A4 con margenes de 40pt en cada lado
        Document doc = new Document(PageSize.A4, 40, 40, 40, 40);

        try {
            //Conecta el documento con el archivo de salida "prematricula.pdf"
            PdfWriter.getInstance(doc, new FileOutputStream("prematricula.pdf"));
            doc.open(); //a partir de aqui ya se puede empezar a agregar contenido

            // --- LOGO ---
            /*Intenta cargar el logo; si no existe el archivo, no truena el
            programa completo (solo se omite el logo), gracias al try/catch propio*/
            try {
                Image logo = Image.getInstance("logo_facultad.jpg");
                logo.scaleToFit(90, 90);//ajuste de tamano maximo 90x90
                logo.setAlignment(Image.ALIGN_CENTER);//lo centra horizontalmente
                doc.add(logo);
            } catch (DocumentException | IOException e) {
                System.out.println("No se pudo cargar el logo, se omite: " + e.getMessage());
            }

            // --- TITULO ---
            //Define una fuente Times Roman, tamano 18, negrita, color azul oscuro
            Font fuenteTitulo = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD, new BaseColor(10, 40, 90));
            Paragraph titulo = new Paragraph("Prematricula - Licenciatura en Ingenieria Informatica", fuenteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);//centra el titulo
            titulo.setSpacingAfter(5);//deja un pequeno espacio debajo
            doc.add(titulo);

            //--- LINEA SEPARADORA ---
            //Dibuja una linea horizontal debajo del titulo, del mismo color azul
            LineSeparator linea = new LineSeparator();
            linea.setLineColor(new BaseColor(10, 40, 90));
            doc.add(new Chunk(linea));
            doc.add(Chunk.NEWLINE); // salto de linea despues de la raya

            //--- DESCRIPCION BREVE ---
            Font fuenteTexto = new Font(Font.FontFamily.TIMES_ROMAN, 11);
            Paragraph descripcion = new Paragraph(
                "A continuacion se detallan las materias habilitadas para el proximo semestre, " +
                "segun los prerrequisitos aprobados por el estudiante.", fuenteTexto
            );
            descripcion.setSpacingAfter(15);
            doc.add(descripcion);

            //--- TABLA ---
            //Tabla de 3 columnas: Codigo, Materia, Semestre
            PdfPTable tabla = new PdfPTable(3);
            tabla.setWidthPercentage(100); // ocupa todo el ancho disponible
            //Ancho relativo de cada columna (la del medio, "Materia", es mas ancha)
            tabla.setWidths(new float[]{1.3f, 3f, 1f});

            //--- ENCABEZADO DE LA TABLA ---
            //Fuente blanca y negrita para que resalte sobre el fondo azul
            Font fuenteEncabezado = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.WHITE);
            String[] encabezados = {"Codigo", "Materia", "Semestre"};

            //Recorre el arreglo de encabezados con for clasico y crea una celda por cada uno
            for (String encabezado : encabezados) {
                PdfPCell celda = new PdfPCell(new Phrase(encabezado, fuenteEncabezado));
                celda.setBackgroundColor(new BaseColor(10, 40, 90));//fondo azul oscuro
                celda.setPadding(8);// espacio interno
                celda.setHorizontalAlignment(Element.ALIGN_CENTER);//texto centrado
                tabla.addCell(celda);
            }

            // --- FILAS DE DATOS ---
            Font fuenteCelda = new Font(Font.FontFamily.TIMES_ROMAN, 11);

            //primeraFila: controla que el nombre/cedula del estudiante se
            // mprima UNA sola vez (en la primera vuelta del while), no en cada fila
            boolean primeraFila = true;

            //filaClara: alterna el color de fondo de cada fila (blanco / azul clarito)
            //para que la tabla sea mas facil de leer, como en Excel
            boolean filaClara = true;

            /*Recorre el ResultSet fila por fila. Aqui SI se usa while,
            porque un ResultSet no tiene indice: .next() es la unica
            forma de avanzar y saber si hay una fila siguiente*/
            while (prematricula.next()) {
					/*Solo en la primera fila: saca cedula y nombre (son los mismos en
					todas las filas porque es el mismo estudiante) y los imprime arriba*/
					if (primeraFila) {
							String cedula = prematricula.getString("cedula");
							String nombre = prematricula.getString("nombre");

							Font fuenteDatos = new Font(Font.FontFamily.TIMES_ROMAN, 11, Font.BOLD);
							Paragraph datosEstudiante = new Paragraph(
								"Estudiante: " + nombre + "     |     Cedula: " + cedula, fuenteDatos
						);
						datosEstudiante.setSpacingAfter(12);
						doc.add(datosEstudiante);
						primeraFila = false; // para que no se repita en las siguientes vueltas
               }//fin del while

                //Elige el color de fondo segun si la fila es "clara" o no
                BaseColor fondoFila;
				if (filaClara) {
				    fondoFila = BaseColor.WHITE;
				} else {
				    fondoFila = new BaseColor(230, 235, 245);
				}

                //Crea las 3 celdas de esta fila con los datos del ResultSet
                PdfPCell c1 = new PdfPCell(new Phrase(prematricula.getString("codigo"), fuenteCelda));
                PdfPCell c2 = new PdfPCell(new Phrase(prematricula.getString("denominacion"), fuenteCelda));
                PdfPCell c3 = new PdfPCell(new Phrase(prematricula.getString("semestre"), fuenteCelda));

                //Aplica el mismo fondo y relleno a las 3 celdas de la fila
                for (PdfPCell c : new PdfPCell[]{c1, c2, c3}) {
                    c.setBackgroundColor(fondoFila);
                    c.setPadding(6);
                }
                c3.setHorizontalAlignment(Element.ALIGN_CENTER);//centra la columna Semestre

                tabla.addCell(c1);
                tabla.addCell(c2);
                tabla.addCell(c3);

                filaClara = !filaClara;//invierte el color para la siguiente fila
            }

            doc.add(tabla);// agrega la tabla completa al documento
            doc.close();//cierra y guarda el archivo PDF

            System.out.println("PDF generado correctamente: prematricula.pdf");

        } catch (DocumentException | SQLException | java.io.IOException e) {
            //Si algo falla (archivo, SQL, o la libreria iText), se imprime el error
        }
    }
}