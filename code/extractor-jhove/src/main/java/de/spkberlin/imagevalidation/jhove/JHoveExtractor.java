package de.spkberlin.imagevalidation.jhove;

import de.spkberlin.imagevalidation.common.extract.ExtractionError;
import de.spkberlin.imagevalidation.common.extract.Extractor;
import edu.harvard.hul.ois.jhove.Module;
import edu.harvard.hul.ois.jhove.ObjectIdentifier;
import edu.harvard.hul.ois.jhove.RepInfo;
import edu.harvard.hul.ois.jhove.module.GifModule;
import edu.harvard.hul.ois.jhove.module.Jpeg2000Module;
import edu.harvard.hul.ois.jhove.module.JpegModule;
import edu.harvard.hul.ois.jhove.module.PdfModule;
import edu.harvard.hul.ois.jhove.module.TiffModule;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JHoveExtractor implements Extractor {

    private static final Logger logger = LoggerFactory.getLogger(JHoveExtractor.class);

    private Module[] modules;
    private ObjectIdentifier identifier;

    public JHoveExtractor() {
        this.modules = new Module[] { new TiffModule(), new Jpeg2000Module(), new JpegModule(), new GifModule(), new PdfModule() };

        this.identifier = new ObjectIdentifier(Arrays.asList(modules));
    }

    public Map<String, Object> extractProperties(Path filepath) throws ExtractionError {
        try {
            RepInfo info = new RepInfo(filepath.toString());
            identifier.identify(filepath.toFile(), info, null, false, false);
            return RepInfoParser.repInfoToPropertyTree(info);
        } catch (Exception e) {
            logger.error("exception while validating with jhove", e);
            throw new ExtractionError("exception while validating with jhove: " + e.getMessage());
        }
    }
}
