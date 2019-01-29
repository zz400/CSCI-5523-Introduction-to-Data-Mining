'''
Created on Nov 24, 2018

@author: zhao
'''

import re
from html.parser import HTMLParser
from porterStemming import PorterStemmer

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
                if key == "TOPICS" and value == "NO":
                    pass
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
                #if parser.newID != 0 and parser.body != "" and topic != "":
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
        print(len(article_list), len(topic_map))
        file1.close()
    
    print(article_list[0])
    sorted_topic = sorted(topic_map.items(), key=lambda kv: kv[1])
    sorted_topic = list(reversed(sorted_topic))
    #print(sorted_topic)
    
    frequentTopics = set()
    for i in range(20):
        frequentTopics.add(sorted_topic[i][0])
    list_articles = [article for article in article_list if article[1] in frequentTopics]
    print("articles: ", len(article_list))
    
    
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
    
    "Prepare token_map:"
    token_map = {}
    
    
    for article in article_list:
        "Pre-processing Step1: Eliminate any non-ascii characters."
        for c in article[2]:
            if ord(c) >= 128:
                #print("Find non-ascii", c, article[0], article[1])
                article[2] = article[2].replace(c, '')
        
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
        words = [word for word in words if word != '']
        words = [word for word in words if word != ' ']
        article[3] = words
        
        "Eliminate tokens from the stop list that is provided."
        words = [word for word in article[3] if word not in stoplist_set]
        article[3] = words
        
        "Obtain the stem of each token using Porter's stemming algorithm:"
        porter = PorterStemmer()
        words = []
        for word in article[3]:
            word = porter.stem(word, 0,len(word)-1)
            words.append(word)
        article[3] = words
    
    for article in article_list:
        for word in article[3]:
            token_map
            if word in token_map:
                x = token_map.get(word)
                token_map[word] = x + 1
            else:
                token_map[word] = 1
    print("tokens: ", len(token_map))
    "Eliminate any tokens that occur less than 5 times."
    token_map = { k:v for k,v in token_map.items() if v >= 5}
    print("tokens: ", len(token_map))
        

        
        
        
         
            
    #print(list_articles[0])
    print(list_articles[0][3])
    print(len(list_articles[0][3]))


