# Stress Test Example

This example dataset is used to stress test the Image Validation CLI. First you needs to generate directories with images using the `generate.sh` or `generate.bat` scripts. Both scripts require 3 parameters:

1. the tif file that is used as a template and is duplicated
2. the number of directories to create
3. the number of image files to create for each directory

For example, use the following command to generate 100 directories with 10 files each using the `template/valid.tif` file, which is considered valid based on the `config.yaml` configuration file:

Linux:

```bash
bash generate.sh template/valid.tif 100 10
```

Windows

```bash
generate.bat template\valid.tif 100 10
```

Once the script is finished, there will be a new directory `generated` that contains 10 duplicates of the template image copied to 100 directories.

Afterwards, you can start the validation of this directory:

Linux:

```
bash spk-iv-cli.sh validate -v --config examples/stress/config.yaml examples/stress
```

Windows:

```
spk-iv-cli.bat validate -v --config examples\stress\config.yaml examples\stress
```
