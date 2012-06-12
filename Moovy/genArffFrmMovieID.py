import sys
import imdb
import re
from imdb import IMDb
sentences = {} #To ensure uniqueness
sentences_freq = {} #To ensure uniqueness
cast = {}
charLineNumber = {}
filepath = "/home/preetam/MalwareAnalyzer/Moovy/"


def genCastDictionary(mid):
    ia = imdb.IMDb()
    p = ia.get_movie(mid)
    #Build a cast dictionary
    dnew = {}
    #Store only the last name of the actor in the dictionary
    for i in p.get('cast'):
        tmp = {}
	if len(i.get('name').split(' ')) == 1:
            cast[i.get('name')] = i.get('name')
	    charLineNumber[i.get('name')] = tmp
        else:
            cast[i.get('name').split(' ')[1]] = i.get('name')
	    dnew[i.get('name').split(' ')[1]] = 0
	    charLineNumber[i.get('name').split(' ')[1]] = tmp
    return dnew

#Sorts a dictionary in descending order
def sortDictionary(TopWords_dict):
    TopWords = [(v, k) for k, v in TopWords_dict.items()]
    TopWords.sort()
    TopWords.reverse()
    TopWords = [(k, v) for v, k in TopWords]
    #printTopWords
    return TopWords

def genCastFrequency(linebyline_array, dictionary, c):
    #print "***",len(linebyline_array)
    for i in linebyline_array:
        if not sentences_freq.has_key(i.strip()):
            sentences_freq[i.strip()] = 1
            tmp = re.findall(r'\w+', i.strip())
            count = 0
            for var in tmp:
                if dictionary.has_key(var):
                    count = dictionary[var]
                    count = count + 1
                    dictionary[var] = count
                    count = 0
                    if charLineNumber.has_key(var):
                        #print var, tmp, c
                        #print var, charLineNumber[var]
                        dct = charLineNumber[var]
                        if not dct.has_key(c+1):
                            dct[c+1] = 1
                        charLineNumber[var] = dct
                        #print "***",var, charLineNumber[var]
            c = c + 1
    #print charLineNumber
    f_castFreq = open(filepath + 'castFreq.txt','w')
    topActors = sortDictionary(dictionary)
    i = iter(topActors)
    while(True):
        try:
            p,q = i.next()
            f_castFreq.write(cast[p].encode('utf-8')+'::'.encode('utf-8')+str(q).encode('utf-8')+'\n'.encode('utf-8'))
        except StopIteration:
            break
    
    f_castFreq.close()
    return c

##def getMovieID(movieIDindex_file):
##    f = open(movieIDindex_file)
##    content = f.readline()
##    if content == "":
##        print "NULL file"
##    return int(content)

def storeCharLineNumber():
    f_charLnumber = open(filepath + "charAppearingOnLines.txt","w")
    for k in charLineNumber.keys():
        for p in charLineNumber[k].keys():
            f_charLnumber.write(k.encode('utf-8')+" ".encode('utf-8')+str(p).encode('utf-8')+"\n".encode('utf-8'))
    f_charLnumber.close()

def genMovieAmazonReviews(movieName, movieIDindex_file):
    File_movie_reviews = open(filepath + "generatedReviews.txt","w")

    #Get the movie ID index from the file
    #mID_index = getMovieID(movieIDindex_file)
    mID_index = int(movieIDindex_file)

    
    # This will by defautl access the web database
    ia = imdb.IMDb()

    
    #Now get the amazon reviews for this movie

    search_results = ia.search_movie(movieName)
    mID = search_results[mID_index].movieID
    cast_dictionary = genCastDictionary(mID)
    amazon_reviews = ia.get_movie_amazon_reviews(mID)

    #Now store the results in the file

    c = 0
    print "Length=",len(amazon_reviews['data']['amazon reviews'])
##    print amazon_reviews['data']['amazon reviews']
    for i in range(0, len(amazon_reviews['data']['amazon reviews'])):
        str1 = amazon_reviews['data']['amazon reviews'][i]['review']
        linebyline = str1.split('.')
        print "@@@",linebyline
##        print "C= ",c
        for line in linebyline:
##            c = c + 1
            if not sentences.has_key(line.strip()):
                if not line.strip() == "":
                    File_movie_reviews.write(line.strip().encode("utf-8")+"|?\n".encode("utf-8"))
                    sentences[line.strip()] = 1
        c = genCastFrequency(linebyline,cast_dictionary, c)
	#File_movie_reviews.write('|')
    storeCharLineNumber()

    File_movie_reviews.close()
    return True

#Stem word using the Lancaster Stemmer algorithm
def stemWord(word):
    from nltk import stem
    stemmer = stem.LancasterStemmer()
    stemmed_word = stemmer.stem(word)
    return stemmed_word

#Read the attributes from the file SortedDictionary_700.txt
def loadDictionary():
    f = open(filepath + 'SortedDictionary_700.txt')
    attributes = {}
    content = f.readline()
    while (content != "" ):
        tmp = content.strip().split(" ")
        attributes[tmp[0]] = tmp[1]
        content = f.readline()
    return attributes

def genHeaderARFF(TopWords):
    file_ARFF = open(filepath + "testset.arff","w")
    file_ARFF.write("@relation popularity\n")
    for var in TopWords.keys():
        temp = "@attribute "+var+" {0,1}"
        file_ARFF.write(temp)
        file_ARFF.write("\n")
    #Positive=1,Negative=0,Neutral=2
    file_ARFF.write("@attribute Sentiment {0,1,2}")
    file_ARFF.write("\n")
    file_ARFF.write("@data")
    file_ARFF.write("\n")
    file_ARFF.close()
    return True

def genDataSegment(TopWords):
    file_train = open(filepath + "generatedReviews.txt","r")
    file_ARFF = open(filepath + "testset.arff","a")
    line = file_train.readline()
    while (line != ""):
        tmp = line.strip().split("|")
        #print tmp
        Sentiment = tmp[1]
        temp = "{"
        TopWord_map = {}
        for var in re.findall(r'\w+', tmp[0]):
            stemmed_word = stemWord(var.lower())
            if not stemmed_word.strip() == "" and not stemmed_word.strip().isdigit() and TopWords.has_key(stemmed_word):
                if not TopWord_map.has_key(TopWords[stemmed_word]):
                    TopWord_map[int(TopWords[stemmed_word])] = 1
        TopWord_Array = TopWord_map.keys()
        TopWord_Array.sort()
        #print TopWord_Array
        for gvar in TopWord_Array:
            temp = temp + str(gvar)+" "+"1, "
        last = len(TopWords)
        #last = last + 1
        temp = temp + str(last) + " "+Sentiment+"}"
        file_ARFF.write(temp)
        file_ARFF.write("\n")
        line = file_train.readline()
    file_train.close()
    file_ARFF.close()
    return True

def genArff(fileName_review):
    attributes = loadDictionary()
    genHeaderARFF(attributes)
    genDataSegment(attributes)
    
if __name__=="__main__":
    #Get a movie as an input
    genMovieAmazonReviews(sys.argv[1],sys.argv[2])
    #Now we have the castFreq.txt and the movie reviews.
    #Generate the ARFF file using the attribute in SortedDictionary_700.txt
    genArff(sys.argv[2])
    
