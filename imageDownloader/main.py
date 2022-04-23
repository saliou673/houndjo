import urllib
import  urllib.request
from PIL import Image
import logging

# Configure logger
logging.getLogger("requests").setLevel(logging.ERROR)
log = logging
log.basicConfig(level=logging.DEBUG, format='%(asctime)s %(levelname)s \t: %(message)s', datefmt='%d/%m/%Y %H:%M:%S')


BASE_URL = 'https://ia903101.us.archive.org/BookReader/BookReaderImages.php'
filename = 'quran-mushaf-almadinah-hafs_0003'
INTPUT_FILE_EXTENSION = 'jp2'
OUTPUT_FILE_EXTENSION = 'jpg'
START_INDEX = 0
END_INDEX = 623

def getFileName(inputPageNumber):
    pageNumber = str(inputPageNumber)
    size = 4 - len(pageNumber)
    for i in range(0, size):
        pageNumber = '0' + pageNumber
    return 'quran-mushaf-almadinah-hafs_' + pageNumber

def getParams(filename):
    return {
        'zip' : '/24/items/quran-mushaf-almadinah-hafs/quran-mushaf-almadinah-hafs_jp2.zip',
        'id' : 'quran-mushaf-almadinah-hafs',
        'scale': 1,
        'rotate': 0,
        'file' : 'quran-mushaf-almadinah-hafs_jp2/' + filename + '.' + INTPUT_FILE_EXTENSION
    }

for i in range(START_INDEX, END_INDEX):
    filename = getFileName(i)
    params = getParams(filename)
    progression = str(i+1) + "/" + str(END_INDEX + 1) + " (" + str(round((100*i)/END_INDEX, 2)) + " %)"
    log.info("Downloading " + filename + "\t" + progression)
    url = BASE_URL + '?' + urllib.parse.urlencode(params)
    response = urllib.request.urlopen(url)
    image = Image.open(response)
    outPutFile = 'images/' + filename  + '.' +  OUTPUT_FILE_EXTENSION
    image.save(outPutFile)