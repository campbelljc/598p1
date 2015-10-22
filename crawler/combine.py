from os import listdir
from os.path import isfile, join
import json
import unicodecsv
import re
from weekday import weekDay
#import Queue

files = [ f for f in listdir("data/") if isfile(join("data/",f)) and '.' not in f ]

articles = []

queue = []

for cf in files:
#    print(cf)
    f = open("data/" + cf)
    article = json.load(f)
    f.close()
#    print(article[14]) # comments
    num_shares = article[13]
    num_comments = article[14]
    add = False
    if isinstance(num_shares, basestring) and 'n/a' in num_shares:
        add = True
    if isinstance(num_shares, int) and num_shares == 0:
        add = True
    if isinstance(num_comments, basestring) and 'n/a' in num_comments:
        add = True
    if isinstance(num_comments, int) and num_comments == 0:
        add = True    
    if len(article) < 17: # doesn't have num. links
        add = True
    if not add:
 #       queue.append([article[0]])
 #   if num_shares == 0 or 'n/a' in num_shares or num_comments == 0 or 'n/a' in num_comments:
  #      queue.append(article)
  #  else:
        articles.append(article)
        
# get category to binary vals.
cats = ['Aboriginal', 'Arts', 'Business', 'Canada', 'Elections', 'Go Public', 'Health', 'Politics', 'Technology', 'Trending', 'World']
art_cats = []
for a in articles:
    newcat = 'None'
    for c in cats:
        if c in a[1]:
            newcat = c
            break
    art_cats.append(newcat) # Note - if it is 'None' then we omit it.

# so for features we say if art_cats[count] not None: features.append([index_of(art_cats[count], cats), ...])

# get num authors
art_num_auth = []
for a in articles:
    num_auth = 1
    if 'None' in a[7]:
        num_auth = 0
    if ', The ' in a[7] or ', special' in a[7]:
        a[7] = a[7].split(', ')[0]
    if ',' in a[7]:
        num_auth = 1 + a[7].count(', ') + a[7].count("and ")
    art_num_auth.append(num_auth)
    
# get sub cat
art_has_sub = []
for a in articles:
    has_sub_cat = 0
    if a[2] != "":
        has_sub_cat = 1
    art_has_sub.append(has_sub_cat)
    
# story flags
art_has_flag = []
for a in articles:
    has_flag = 0
    if a[3] != "":
        print a[3]
        has_flag = 1
    art_has_flag.append(has_flag)
    
# num numbers in title
art_num_nums = []
for a in articles:
    num_nums = 0
    a[4] = a[4].replace(',', '')
    words = a[4].split(' ')
    for w in words:
        w = w.replace('%', '')
        w = w.replace('$', '')
        if w.isdigit():
            num_nums = num_nums + 1
    art_num_nums.append(num_nums)
    s = re.findall(r'\d+', a[4])
    num_nums = len(s)
    art_num_nums.append(num_nums)
    
# get avg length word title
art_avg_title = []
for a in articles:
    words = a[4].split(' ')
    totalWordLength = 0
    for w in words:
        totalWordLength = totalWordLength + len(w)
    avg_title = totalWordLength / len(words)
    art_avg_title.append(avg_title)
    
# get weekday/weekend
art_weekdays = []
art_weekends = []
months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
for a in articles:
    month = a[8][0:3]
    day = a[8][4:6]
    year = a[8][8:12]
    month_num = months.index(month) + 1
    ret = weekDay(int(year), int(month_num), int(day))
    weekday = 0
    weekend = 0
    if ret[0] > 0 and ret[0] < 6:
        weekday = 1
    if ret[0] == 0 or ret[0] == 6:
        weekend = 1
    art_weekdays.append(weekday)
    art_weekends.append(weekend)
    
# get morning, afternoon, evening...
art_morning = []
art_afternoon = []
art_evening = []
for a in articles:
    time = a[8][13:]
    time = time[:len(time)-3]
    time_split = time.split(':')
    am_pm = time_split[1][3:]
    hour = int(time_split[0])
    morning = 0
    afternoon = 0
    evening = 0
    if ('AM' in am_pm and hour > 4):
        morning = 1
    elif ('PM' in am_pm and (hour < 6 or hour == 12)):
        afternoon = 1
    else:
        evening = 1
    art_morning.append(morning)
    art_afternoon.append(afternoon)
    art_evening.append(evening)
    
features = []

count = 0
for a in articles:
    if ('None' in art_cats[count]):
        count = count + 1
        continue
    features.append([   a[0], # url
                        a[4], # title
                        1 if cats[0] in art_cats[count] else 0,
                        1 if cats[1] in art_cats[count] else 0,
                        1 if cats[2] in art_cats[count] else 0,
                        1 if cats[3] in art_cats[count] else 0,
                        1 if cats[4] in art_cats[count] else 0,
                        1 if cats[5] in art_cats[count] else 0,
                        1 if cats[6] in art_cats[count] else 0,
                        1 if cats[7] in art_cats[count] else 0,
                        1 if cats[8] in art_cats[count] else 0,
                        1 if cats[9] in art_cats[count] else 0,
                        1 if cats[10] in art_cats[count] else 0,
                        art_has_sub[count],
                        art_has_flag[count],
                        a[5], # num words in title
                        art_num_nums[count],
                        art_avg_title[count],
                        art_num_auth[count],
                        art_morning[count],
                        art_afternoon[count],
                        art_evening[count],
                        art_weekdays[count],
                        art_weekends[count],
                        a[9],  # num sub headlines
                        a[10], # num paragraphs
                        a[11], # num inline figures
                        a[12], # num videos
                        a[16], # num links
                        a[13], # num shares
                        a[14]  # num comments
                    ])
    count = count + 1

# the actual features
features.insert(0, ['url', 'title', 'isAboriginal', 'isArts', 'isBusiness', 'isCanada', 'isElections', 'isGoPublic', 'isHealth', 'isPolitics', 'isTech', 'isTrending', 'isWorld', 'hasSubCategory', 'hasStoryFlag', 'numWordsTitle', 'numNumsTitle', 'avgLengthWordsTitle', 'numAuthors', 'wasMorning', 'wasAfternoon', 'wasEvening', 'wasWeekday', 'wasWeekend', 'numSubHeadlines', 'numParagraphs', 'numInlineFigures', 'numVideos', 'numLinks', 'numShares', 'numComments'])

#articles.insert(0, ['URL', 'Category', 'Subcategory', 'Story flag', 'Story title', 'Num. words in title', 'Description', 'Author', 'Post date', 'Num. sub-headlines', 'Num. paragraphs', 'Num. inline figures', 'Num. videos', 'Num. shares', 'Num. comments', 'Crawler version', 'Num. links'])

with open("cbc.csv", "w") as f:
    writer = unicodecsv.writer(f)
    writer.writerows(features)
    
print(len(features))
print(len(features[0]))
    
#f = open("queue2", "w")
#json.dump(queue, f)
#f.close()