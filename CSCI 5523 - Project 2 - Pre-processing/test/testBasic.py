'''
Created on Nov 24, 2018

@author: zhao
'''
import re

if __name__ == '__main__':
    list1 = ['', 'showers', 'continued','', '', '332', '3', '4', 'throughout', "aa"]
    print(list1)
    print(len(list1))
    list1 = [x for x in list1 if not x.isdigit()]
    list1 = [x for x in list1 if x != '']
    
#     for word in list1:
#         print(word)
#         if word.isdigit():
#             list1.remove(word)
    print(list1)
    print(len(list1))
    
    mydict = {'one': 1, 'two': 2, 'three': 3, 'four': 4}
    mydict = { k:v for k,v in mydict.items() if v != 3 }
    print(mydict)
    
    
    ll = [1, 2]
    classfile = open("reuters21578.class", "w")
    line = str(ll[0]) + "," + str(ll[1])
    classfile.write(line)
    
    norm = [float(i)/sum(i*i) for i in ll]
    print(norm)
    #print([float(i) / sum(i*i) for i in ll])