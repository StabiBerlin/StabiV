package de.spkberlin.imagevalidation.jhove;

import de.spkberlin.imagevalidation.common.extract.ExtractionError;
import de.spkberlin.imagevalidation.common.extract.ExtractionResult;
import de.spkberlin.imagevalidation.common.extract.Extractor;
import edu.harvard.hul.ois.jhove.App;
import edu.harvard.hul.ois.jhove.JhoveBase;
import edu.harvard.hul.ois.jhove.JhoveException;
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
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JHoveExtractor implements Extractor {

    private static final Logger logger = LoggerFactory.getLogger(JHoveExtractor.class);

    private static final App app = App.newAppWithName("JHOVE");

    private Module[] modules;
    private ObjectIdentifier identifier;
    private JhoveBase jhove;

    /**
     * Initialize a new JHove instance.
     */
    public JHoveExtractor() {
        try {
            modules = new Module[] { new TiffModule(), new Jpeg2000Module(), new JpegModule(), new GifModule(), new PdfModule() };
            identifier = new ObjectIdentifier(Arrays.asList(modules));
            jhove = new JhoveBase();
            jhove.setEncoding("utf-8");
            for (Module module : modules) {
                jhove.getModuleMap().put(module.getName().toLowerCase(), module);
            }
        } catch (JhoveException e) {
            throw new RuntimeException("failed initialize JHove", e);
        }
    }

    /**
     * Extract image properties for an image file.
     *
     * @param filepath the filepath to the image file whose properties are supposed to be extracted
     * @return the extracted property tree
     */
    public ExtractionResult extractProperties(Path filepath) throws ExtractionError {
        try {
            RepInfo info = new RepInfo(filepath.toString());
            identifier.identify(filepath.toFile(), info, null, false, true);

            if (Objects.nonNull(info.getModule())) {
                // file type has been identified, do detailed parsing
                Module module = info.getModule();
                info = new RepInfo(filepath.toString());
                jhove.processFile(app, module, false, filepath.toFile(), info);
            }

            return new ExtractionResult(RepInfoParser.repInfoToPropertyTree(info));
        } catch (Exception e) {
            logger.error("exception while validating with jhove", e);
            throw new ExtractionError("exception while validating with jhove: " + e.getMessage());
        }
    }
}
