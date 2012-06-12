import sys
import imdb
from imdb import IMDb



def genMovieAmazonReviews(movieName):
    movies = open("/home/preetam/MalwareAnalyzer/Moovy/search_results.txt","w")

    # This will by defautl access the web database
    ia = imdb.IMDb()

    #Search the movie name and get its corresponding ID
    search_results = ia.search_movie(movieName)

    count = 0
    for item in search_results:
            movies.write(item['long imdb canonical title'].encode('utf-8'))
            movies.write(" ".encode('utf-8')+str(count).encode('utf-8'))
            movies.write('\n')
            count = count +1
   

if __name__=="__main__":
    #Get a movie as an input
    genMovieAmazonReviews(sys.argv[1])
  
    
