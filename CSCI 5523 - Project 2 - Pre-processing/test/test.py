'''
Created on Nov 24, 2018

@author: zhao
'''

import re
from html.parser import HTMLParser

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
    article_set = set()
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
                    article_set.add(parser.newID)
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
    article_list1 = [article for article in article_list if article[1] in frequentTopics_set]
    for  article in article_list:
        if article[1] not in frequentTopics_set:
            article_set.remove(article[0])
    
    print("Number of articles selected: ", len(article_list))
    print(article_set)
    print(len(article_set))
    print(1916 in article_set)
    print(7952 in article_set)
    print(13056 in article_set)
    print(21254 in article_set)
    print(21491 in article_set)
    
