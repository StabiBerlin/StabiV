package de.spkberlin.imagevalidation.cli.configuration.state;

public class ConfigurationProcessingSection {

    private Integer threads;
    private String checksumAlgorithm;

    public ConfigurationProcessingSection() {
        this.threads = 4;
        this.checksumAlgorithm = "SHA-1";
    }

    public Integer getThreads() {
        return this.threads;
    }

    public void setThreads(Integer parallelThreads) {
        this.threads = parallelThreads;
    }

    public String getChecksumAlgorithm() {
        return this.checksumAlgorithm;
    }

    public void setChecksumAlgorithm(String checksumAlgorithm) {
        this.checksumAlgorithm = checksumAlgorithm;
    }
}
