'''
Created on Nov 24, 2018

@author: zhao
'''

import re
import math
from html.parser import HTMLParser
from porterStemming import PorterStemmer
#import nltk

class MyParser(HTMLParser):
    def __init__(self):
        HTMLParser.__init__(self)
        self.newID = 0
        self.topic = ""
        self.body = ""
        self.isSingleTopic = False
        #self.lastTag = ""
        self.currTag = ""

    def handle_starttag(self, tag, attrs):
        self.currTag = tag
        if tag == "reuters":
            for (key, value) in attrs:
                if key == "topics" and value == "NO":
                    break
                if key == "newid":
                    self.newID = int(value)

    def handle_data(self, data):
        if self.newID != 0 and self.currTag == "body" and data != "\n":
            self.body = data


if __name__ == '__main__':
    re_topics_type1 = re.compile("<TOPICS><D>\w+</D></TOPICS>")
    re_topics_type2 = re.compile("<TOPICS><D>\w+\-\w+</D></TOPICS>")
    file_dict = "reuters21578/"
    article_list = []
    topic_map = {}
    print("Selecting the subset of the dataset...")
    
    for file_index in range(22):
        file_name = "reut2-" + "%03d" % file_index + ".sgm"
        file_path = file_dict + file_name
        file1 = open(file_path, encoding="latin-1") 
        text = ""
        topic = ""
        for line in file1.readlines():
            if line.startswith("<REUTERS TOPICS="):
                text = ""
                topic = ""
            text += line
            
            if line.startswith("<TOPICS>"):
                if re_topics_type1.match(line) or re_topics_type2.match(line):
                    topic = line.replace('<TOPICS><D>','')
                    topic = topic.replace('</D></TOPICS>','')
                    topic = topic.replace('\n','')
                else:
                    continue
            
            if line == "</REUTERS>" or line == "</REUTERS>\n" :
                parser = MyParser()
                parser.feed(text)
                parser.close()
                if parser.body != "" and topic != "":
                    parser.topic = topic
                    article = [parser.newID, parser.topic, parser.body]
                    article_list.append(article)
                    if parser.topic in topic_map:
                        x = topic_map.get(parser.topic)
                        topic_map[parser.topic] = x + 1
                    else:
                        topic_map[parser.topic] = 1
                    #print("newID: %d; topic: " %parser.newID, parser.topic)
        #print(len(article_list), len(topic_map))
        file1.close()
    
    #print(article_list[0])
    sorted_topic = sorted(topic_map.items(), key=lambda kv: kv[1])
    sorted_topic = list(reversed(sorted_topic))
    #print(sorted_topic)
    
    frequentTopics_set = set()
    for i in range(20):
        frequentTopics_set.add(sorted_topic[i][0])
    article_list = [article for article in article_list if article[1] in frequentTopics_set]
    print("Number of articles selected: ", len(article_list))
    
    
    "======================================================="
    
    print("\nDo data pre-processing (9 steps) ...")
    
    "Prepare stoplist_set:"
    stoplist_set = set()
    stoplist_filename = "stoplist.txt"
    stopfile = open(stoplist_filename)
    for line in stopfile.readlines():
        line = line.replace('\n', '')
        words = line.split(" ")
        for word in words:
            stoplist_set.add(word)
    stopfile.close()
    stoplist_set = sorted(stoplist_set)
    #print("stop-list: ", len(stoplist_set))
    #print("stop-list: ", stoplist_set)
    
    for article in article_list:
        "Eliminate any non-ascii characters."
        for c in article[2]:
            if ord(c) >= 128:
                #print("Find non-ascii", c, article[0], article[1])
                article[2] = article[2].replace(c, ' ')
        
        "Parse character entities such as '&lt' and '&ge'."
        if "&lt" in article[2]:
            #print("Find '&lt': ", article[0], article[1])
            article[2] = article[2].replace("&lt", "<")
            
        if "&ge" in article[2]:
            #print("Find '&ge': ", article[0], article[1])
            article[2] = article[2].replace("&ge", ">")
        
        "String to lowercase."    
        article[2] = article[2].lower()
        
        "Replace any non alphanumeric characters with space."
        article[2] = re.sub('[^0-9a-zA-Z]+', ' ', article[2])
        
        "Split the text into tokens, using space as the delimiter."
        words = article[2].split(" ")
        article.append(words)
        
        "Eliminate any tokens that contain only digits."
        words = [word for word in article[3] if not word.isdigit()]
        #words = [word for word in words if word != '']
        #words = [word for word in words if word != ' ']
        article[3] = words
        
        "Eliminate tokens from the stop list that is provided."
        words = [word for word in article[3] if word not in stoplist_set]
        article[3] = words
        
        "Obtain the stem of each token using Porter's stemming algorithm:"
        porter = PorterStemmer()
        words = []
        for word in article[3]:
            word = porter.stem(word, 0,len(word)-1)
            #word = porter.stem(word)
            words.append(word)
        article[3] = words
    
    token_map = {}
    for article in article_list:
        for word in article[3]:
            if word in token_map:
                x = token_map.get(word)
                token_map[word] = x + 1
            else:
                token_map[word] = 1
    #print("tokens: ", len(token_map))
    
    "Eliminate any tokens that occur less than 5 times."
    token_map = { k:v for k,v in token_map.items() if v >= 5}
    print("Number of tokens (dimensions): ", len(token_map))
        
    sorted_token = sorted(token_map.items(), key=lambda kv: kv[1])
    sorted_token = list(reversed(sorted_token))
    
    #for item in sorted_token:
        #print(item[0])
    
#     numToken = 0    
#     for k,v in token_map.items():
#         for c in k:
#             if c.isdigit():
#                 numToken += 1
#                 break
#     print(numToken)
        
    #print(list_articles[0])
    #print(article_list[0][3])
    #print(len(article_list[0][3]))
    
    
    "======================================================="
    
    print("\nGenerate vector representation and files...")
    
    "article[0]: newID (int)"
    "article[1]: topic"
    "article[2]: body (string)"
    "article[3]: token list (list of strings)"
    "article[4]: map tokens in article[3] to dimension # (list of int)"
    "                Filling in -1 if the token isn't frequent"
    "article[5]: non-zero dimension vector (list of int)"
    "article[6]: normalized freq vector (list of int)"
    "article[7]: normalized sqrtFreq vector (list of int)"
    "article[8]: normalized log2Freq vector (list of int)"
    
    token2dim_map = {}
    
    "Generate the class file."
    classfile = open("reuters21578.class", "w")
    for article in article_list:
        line = str(article[0]) + "," + str(article[1]) + '\n'
        classfile.write(line)
    classfile.close()
    print("Created 'reuters21578.class'.")
    
    "Generate the label file."
    labelfile = open("reuters21578.clabel", "w")
    index = 0
    for tokenItem in sorted_token:
        token = tokenItem[0]
        line = str(index) + "," + str(token) + '\n'
        labelfile.write(line)
        token2dim_map[token] = index
        index += 1
    labelfile.close()
    print("Created 'reuters21578.clabel'.")
    
    "Generate the three input files."
    for article in article_list:
        token_list = article[3]
        dim_list = []
        freq_map = {}
        dim_vector = []
        freq_vector = []
        sqrtfreq_vector = []
        log2freq_vector = []
        for token in token_list:
            if token in token2dim_map:
                dim = token2dim_map[token]
                dim_list.append(dim)
            else:
                dim_list.append(-1)
        article.append(dim_list)  
        
        for dim in dim_list:
            if dim == -1:
                continue
            if dim in freq_map:
                x = freq_map[dim]
                freq_map[dim] = x + 1
            else:
                freq_map[dim] = 1
        sorted_freq = sorted(freq_map.items(), key=lambda kv: kv[0])
        sorted_freq = list(sorted_freq)
        
        for item in sorted_freq:
            dim_vector.append(item[0])
            freq_vector.append(item[1])
            sqrtfreq_vector.append(math.sqrt(item[1]) + 1)
            log2freq_vector.append(math.log(item[1], 2) + 1)
        article.append(dim_vector)  
        mag = math.sqrt(sum(i**2 for i in freq_vector))
        article.append([float(i) / mag for i in freq_vector])  
        mag = math.sqrt(sum(i**2 for i in sqrtfreq_vector))
        article.append([float(i) / mag for i in sqrtfreq_vector])  
        mag = math.sqrt(sum(i**2 for i in log2freq_vector))
        article.append([float(i) / mag for i in log2freq_vector])  
        
        if len(article[5]) <= 0:
            print(article[0], article[3])
            print(article[0], article[4])
            print(article[0], article[5])
            print(article[0], article[6])
            print(article[0], article[7])
            print(article[0], article[8], '\n')
    
    inputfile1 = open("freq.csv", "w")
    inputfile2 = open("sqrtfreq.csv", "w")
    inputfile3 = open("log2freq.csv", "w")    
    for article in article_list:
        newID = article[0]
        for i in range(len(article[5])):
            dim = article[5][i]
            freq = article[6][i]
            sqrtFreq = article[7][i]
            log2Freq = article[8][i]
            line1 = str(newID) + "," + str(dim) + "," + str(freq) + '\n'
            line2 = str(newID) + "," + str(dim) + "," + str(sqrtFreq) + '\n'
            line3 = str(newID) + "," + str(dim) + "," + str(log2Freq) + '\n'
            inputfile1.write(line1)
            inputfile2.write(line2)
            inputfile3.write(line3)
    inputfile1.close()
    inputfile2.close()
    inputfile3.close()
    print("Created 'freq.csv'.")
    print("Created 'sqrtfreq.csv'.")
    print("Created 'log2freq.csv'.")
        
            
        
        
    
    
    
    


