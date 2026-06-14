package de.spkberlin.stabiv.common.checksum;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Hex;

public class ChecksumUtil {

    /**
     * Calculate the checksum of a file and return the calculated digest as string.
     *
     * <p>Possible Algorithms are listed here:
     * https://docs.oracle.com/en/java/javase/21/docs/specs/security/standard-names.html#messagedigest-algorithms
     *
     * @param file the file to calculate a checksum for
     * @param algorithm the checksum algorithm to use
     * @return the checksum as string
     * @throws IOException in case reading the file fails
     * @throws NoSuchAlgorithmException in case the requested algorithm is not available
     */
    public static String calculate(Path file, String algorithm) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);

        try (InputStream is = Files.newInputStream(file); DigestInputStream dis = new DigestInputStream(is, digest)) {
            byte[] buffer = new byte[8192];
            while (dis.read(buffer) != -1) {
                // reading updates digest automatically
            }
        }

        return Hex.encodeHexString(digest.digest());
    }
}
