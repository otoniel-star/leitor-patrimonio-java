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

public class LeitorMotor {

    // Retorna um array no formato: [NomeDaFoto, Patrimonio, NumeroDeSerie]
    public static String[] processarImagem(File arquivoImagem) {
        String patrimonio = "Não encontrado";
        String numeroSerie = "Não encontrado";
        String nomeFoto = arquivoImagem.getName();

        try {
            BufferedImage bufferedImage = ImageIO.read(arquivoImagem);
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

            MultipleBarcodeReader leitorMultiplo = new GenericMultipleBarcodeReader(new MultiFormatReader());
            Result[] resultados = leitorMultiplo.decodeMultiple(bitmap, hints);

            for (Result resultado : resultados) {
                String textoLido = resultado.getText();
                if (textoLido.matches(".*[a-zA-Z]+.*")) {
                    numeroSerie = textoLido;
                } else {
                    patrimonio = textoLido;
                }
            }
        } catch (NotFoundException e) {
            // Apenas ignora e retorna "Não encontrado" nas variáveis
        } catch (Exception e) {
            patrimonio = "Erro na leitura";
            numeroSerie = "Erro na leitura";
        }

        return new String[]{nomeFoto, patrimonio, numeroSerie};
    }
}