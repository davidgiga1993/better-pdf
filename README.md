# better-pdf

This project is intended for providing common pdf functionality such as merging duplex scanned pages into one pdf.

Currently only the functionality for which I had a need for has been implemented but feel free to open feature
requests.

## Background
It uses the `sejda` pdf engine and can watch filesystem changes to automatically trigger an action as soon as new files 
are dropped into a folder.

This allows the tool to be hosted on a nas or similar while the end-user just has to drop files into a folder.

## Requirements
Either `docker` or `java 11 (or later)`

## Usage
Watches a shared folder for new duplex scanned pdfs and merges them.
```bash
/opt/better-pdf/bin/better-pdf duplex-watch --source /media/shares/scanns/duplex --output /media/shares/scanns/duplex/out.pdf
```


## Terminology
By `duplex scanned pdfs` I'm referring to two pdf files that get created when scanning
a single document on both sides.
PDF 1 contains all front pages, while PDF 2 contains the backsides in inverted order:
```
# Example of a 6 page duplex scanned document
PDF1: Page 1, Page 3, Page 5
PDF2: Page 6, Page 4, Page 2
```