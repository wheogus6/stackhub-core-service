package core.utill;


import core.dto.CodeCreateInfoDto;
import core.repository.CodeCreateInfoRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class CommonUtill {

    @Autowired
    CodeCreateInfoRepositoryCustom codeCreateInfoRepositoryCustom;

    public String createNewCode(String category) {
        CodeCreateInfoDto dto = codeCreateInfoRepositoryCustom.getCodeInfo(category);
        String code = dto.getCode();
        String num = dto.getNumber();

        Integer numValue = Integer.parseInt(num);
        numValue++;
        String updateNum = String.format("%06d", numValue);

        codeCreateInfoRepositoryCustom.updateNumber(category, updateNum);

        String newCode = code + updateNum;

        return newCode;
    }

    public String addDashForTelNo(String number) {
        String formattedNumber = "";
        if (number.length() == 11) {
            formattedNumber = number.substring(0, 3) + "-" + number.substring(3, 7) + "-" + number.substring(7);
        } else if (number.length() == 10) {
            formattedNumber = number.substring(0, 3) + "-" + number.substring(3, 6) + "-" + number.substring(6);
        } else if (number.length() == 9) {
            formattedNumber = number.substring(0, 2) + "-" + number.substring(2, 5) + "-" + number.substring(5);
        } else if (number.length() == 8) {
            formattedNumber = number.substring(0, 4) + "-" + number.substring(4, 8);
        } else {
            formattedNumber = number;
        }
        return formattedNumber;
    }

    public String dateFormatter(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String changeDate = date.format(formatter);
        return changeDate;
    }

    public String dateAndTimeFormatter(LocalDateTime date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        String changeDate = date.format(formatter);
        return changeDate;
    }

    public static byte[] bufferedImageToByteArray(BufferedImage image, String format) throws IOException, IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }

}
