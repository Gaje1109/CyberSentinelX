import fitz

def extractText_From_PDF(file_path):
    pdfdoc = fitz.open(file_path)
    pdfcontent = ""
    for page in pdfdoc:
        pdfcontent += page.get_text()
        
    return pdfcontent