package org.example;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.multi.MultipleBarcodeReader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.EnumMap;
import java.util.Map;

public class App {
    public static void main(String[] args) {

        File arquivoImagem = new File("C:\\Users\\otonielsilva\\Downloads\\testeleitura01.jpeg");

        if (arquivoImagem.exists()) {
            try {
                BufferedImage bufferedImage = ImageIO.read(arquivoImagem);
                LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
                hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

                MultipleBarcodeReader leitorMultiplo = new GenericMultipleBarcodeReader(new MultiFormatReader());
                Result[] resultados = leitorMultiplo.decodeMultiple(bitmap, hints);

                // Variáveis para guardar os valores encontrados
                String patrimonio = "Não encontrado";
                String numeroSerie = "Não encontrado";

                // Varre os resultados e distribui para as variáveis corretas
                for (Result resultado : resultados) {
                    String textoLido = resultado.getText();
                    if (textoLido.matches(".*[a-zA-Z]+.*")) {
                        numeroSerie = textoLido;
                    } else {
                        patrimonio = textoLido;
                    }
                }

                // IMPRESSÃO FINAL NO FORMATO DESEJADO
                System.out.println(arquivoImagem.getName()); // Imprime o nome do arquivo (ex: foto1.jpg)
                System.out.println("patrimonio : " + patrimonio);
                System.out.println("numero de serie: " + numeroSerie);

            } catch (NotFoundException e) {
                System.out.println(arquivoImagem.getName());
                System.out.println("Erro: Nenhum código nítido encontrado na imagem.");
            } catch (Exception e) {
                System.err.println("Erro inesperado: " + e.getMessage());
            }
        } else {
            System.err.println("Arquivo não encontrado!");
        }
    }
}