from flask import Flask, render_template, request, make_response
from werkzeug import secure_filename
import face_recognition
import sqlite3
import numpy as np
import pickle
import cv2
from datetime import date
import os
import time
import datetime
import train
import knn
import threading
import shutil

app = Flask(__name__)

#CREATE DATABASE AND TABLE
conn = sqlite3.connect("faces.db")

conn.execute(
'''
	CREATE TABLE IF NOT EXISTS admin (
	username TEXT PRIMARY KEY, 
	password TEXT);
'''
)

conn.execute(
'''
	CREATE TABLE IF NOT EXISTS student (
	id INTEGER PRIMARY KEY AUTOINCREMENT, 
	name TEXT, 
	rollno TEXT, 
	semester TEXT,
	course TEXT, 
	session TEXT, 
	UNIQUE(rollno, semester, course, session));
'''
)

conn.execute(
'''
	CREATE TABLE IF NOT EXISTS attendance (
	id	INTEGER PRIMARY KEY AUTOINCREMENT,
	studentid	INTEGER,
	attended	INTEGER,
	attenddate	TEXT,
	UNIQUE(studentid , attenddate),
	foreign key(studentid) references student(id) on delete cascade
);
'''
)

conn.close()

#FACE COUNT
def getfacecount(img):
   facedata = "haarcascade_frontalface_alt.xml"
   cascade = cv2.CascadeClassifier(facedata)
   minisize = (img.shape[1], img.shape[0])
   miniframe = cv2.resize(img, minisize)
   faces = cascade.detectMultiScale(miniframe)
   count = len(faces)   
   return count

#FACE FROM IMAGE
def getfacefromimage(img):
   facedata = "haarcascade_frontalface_alt.xml"
   cascade = cv2.CascadeClassifier(facedata)
   minisize = (img.shape[1], img.shape[0])
   miniframe = cv2.resize(img, minisize)
   faces = cascade.detectMultiScale(miniframe)
   
   for f in faces:
      x, y, w, h = [v for v in f]
      cv2.rectangle(img, (x, y), (x + w, y + h), (255, 255, 255))
      sub_face = img[y:y + h, x:x + w]
   return sub_face

#FACE EXISTS OR NOT
def faceexists(img):
   exists = False
   encoding = face_recognition.face_encodings(img)[0]
   conn = sqlite3.connect('faces.db')
   cursor = conn.execute("SELECT * from student")
   for row in cursor:
      knownencoding = pickle.loads(row[6])
      results = face_recognition.compare_faces([knownencoding], encoding)
      if results[0] == True:
         exists = True
   conn.close()
   return exists

#SEARCH USERS
def search(q):
   conn = sqlite3.connect('faces.db')
   c = conn.execute('SELECT * from users WHERE name LIKE ?', ('%'+ q +'%',))
   jsonstring = "["
   for row in c:
      userid = row[0]
      name = row[1]
      #encoding = row[2]
      jsonstring = jsonstring + '{"id" : "%s", "name" : "%s"},' % (userid,name)  
   jsonstring = jsonstring[:-1] + ']'
   return jsonstring

#TRAIN
def starttraining():
   train.train("static/train", model_save_path="static/trained_knn_model.clf", n_neighbors=2)
   return 1

#--------------------------STUDENT CODE-------------------------------#

#--------------------------ANDROID APP CODE---------------------------#
def updatestudentdetailsfromapp(studentid, name, rollno, semester, course, session):
   conn = sqlite3.connect('faces.db')
   row = (name, rollno, semester, course, session, studentid)
   try:
      conn.execute('UPDATE student SET name=?,rollno=?,semester=?,course=?,session=? WHERE id=?', row)
   except sqlite3.Error as e:
      return '[{"success" : "0","error" : "'+str(e)+'"}]'
   conn.commit()
   return '[{"success" : "1","error" : "no error"}]'

@app.route('/apploginsubmit', methods = ['GET', 'POST'])
def apploginsubmit():
   if request.method == 'POST':
      username = request.form['username']
      password = request.form['password']
      ousername = ""
      opassword = ""
      conn = sqlite3.connect('faces.db')
      c = conn.execute('SELECT * from admin WHERE username = ?', (username,))
      for row in c:
         ousername = row[0]
         opassword = row[1]
      
      if opassword == password:
         return '[{"loginstatus":"1"}]'
      else:
         return '[{"loginstatus":"0"}]'

@app.route('/appstudentinfofromimage', methods = ['GET', 'POST'])
def appstudentinfofromimage():
   if request.method == 'POST':
      f = request.files['fileToUpload']
      f.save('static/temp.jpg')
      full_file_path = 'static/temp.jpg'

      founduserid = 'null'
      foundname = 'null'
      foundrollno = 'null'
      foundsemester = 'null'
      foundcourse = 'null'
      foundsession = 'null'
      try:
         predictions = knn.predict(full_file_path, model_path="static/trained_knn_model.clf")
         founduserid = knn.getName(predictions)
         conn = sqlite3.connect('faces.db')
         row = (founduserid,)
         cursor = conn.execute("SELECT * from student WHERE id = ?", row)
         for row in cursor:
            foundname = row[1]
            foundrollno = row[2]
            foundsemester = row[3]
            foundcourse = row[4]
            foundsession = row[5]
      except:
         '[{"id" : "%s", "name" : "%s", "rollno" : "%s", "semester" : "%s", "course" : "%s", "session" : "%s"}]' % (founduserid, foundname, foundrollno, foundsemester, foundcourse, foundsession)
   return '[{"id" : "%s", "name" : "%s", "rollno" : "%s", "semester" : "%s", "course" : "%s", "session" : "%s"}]' % (founduserid, foundname, foundrollno, foundsemester, foundcourse, foundsession)

@app.route('/appinsertattendance', methods = ['GET', 'POST'])
def appinsertattendance():
   if request.method == 'POST' or request.method == 'GET':
      studentid = request.args.get("studentid")
      conn = sqlite3.connect("faces.db")
      row = (str(studentid), str(1), date.today())
      try:
         conn.execute("INSERT INTO attendance (studentid, attended,attenddate)values(?,?,?)", row)
      except sqlite3.Error as e:
         #2 for already inseted today
         return '2'
      conn.commit()
   return "1"

@app.route('/appaddstudent', methods = ['GET', 'POST'])
def appaddstudent():
   if request.method == 'POST' or request.method == 'GET':
      name = request.args.get('name')
      rollno = request.args.get('rollno')
      semester = request.args.get('semester')
      course = request.args.get('course')
      session = request.args.get('session')
      conn = sqlite3.connect("faces.db")
      row = (name, rollno, semester, course, session,)
      try:
         conn.execute('INSERT INTO student (name,rollno,semester,course,session)values(?,?,?,?,?)', row)
      except sqlite3.Error as e:
         return '[{"success" : "0","error" : "'+str(e)+'"}]'
      conn.commit()
      return '[{"success" : "1","error" : "no error"}]'

@app.route('/appupdatestudentdetails', methods = ['GET', 'POST'])
def appupdatestudentdetails():
   if request.method == 'POST' or request.method == 'GET':
      #f = request.files['fileToUpload']
      studentid = request.form['studentid']
      name = request.form['name']
      rollno = request.form['rollno']
      semester = request.form['semester']
      course = request.form['course']
      session = request.form['session']
      #f.save('file/' + secure_filename(f.filename))
      return updatestudentdetailsfromapp(studentid=studentid, name=name, rollno=rollno, semester=semester, course=course,session=session)

'''
@app.route('/appupdatestudentimage', methods = ['GET', 'POST'])
def appupdatestudentimage():
   if request.method == 'POST' or request.method == 'GET':
      f = request.files['fileToUpload']
      studentid = request.args.get('studentid')
      f.save('file/' + secure_filename(f.filename))
      return updatestudentimagefromapp(path='file/' + secure_filename(f.filename),studentid=studentid)
'''

@app.route('/appsearchstudent', methods = ['GET', 'POST'])
def appsearchstudent():
   q = ""
   if request.method == 'POST' or request.method == 'GET':
      w = request.args.get("with")
      q = request.args.get("q")

   conn = sqlite3.connect("faces.db")

   if w == 'studentid':
      sql = "SELECT * from student WHERE id LIKE '%" + str(q) + "%'"
   elif w == 'name':
      sql = "SELECT * from student WHERE name LIKE '%" + str(q) + "%'"
   elif w == 'rollno':
      sql = "SELECT * from student WHERE rollno LIKE '%" + str(q) + "%'"
   elif w == 'semester':
      sql = "SELECT * from student WHERE semester LIKE '%" + str(q) + "%'"
   elif w == 'course':
      sql = "SELECT * from student WHERE course LIKE '%" + str(q) + "%'"
   elif w == 'session':
      sql = "SELECT * from student WHERE session LIKE '%" + str(q) + "%'"
   else:
      sql = "SELECT * from student"

   cursor = conn.execute(sql)
   #data = cursor.fetchall()

   found = False

   jsonstr = '['
   for row in cursor:
      found = True
      id = row[0]
      name = row[1]
      rollno = row[2]
      semester = row[3]
      course = row[4]
      session = row[5]
      jsonstr = jsonstr + '{"studentid":"'+str(id)+'", "name":"'+name+'", "rollno":"'+str(rollno)+'", "semester":"'+str(semester)+'", "course":"'+str(course)+'", "session":"'+str(session)+'"},'

   if found == True:
      jsonstr = jsonstr[:-1] + ''

   jsonstr = jsonstr + ']'

   return str(jsonstr)

@app.route('/appgetstudentinfo', methods = ['GET', 'POST'])
def appgetstudentinfo():
   q = ""
   if request.method == 'POST' or request.method == 'GET':
      q = request.args.get("q")

   conn = sqlite3.connect("faces.db")
   sql = "SELECT * from student WHERE id = " + str(q) + ""

   cursor = conn.execute(sql)
   #data = cursor.fetchall()

   found = False

   jsonstr = '['
   for row in cursor:
      found = True
      id = row[0]
      name = row[1]
      rollno = row[2]
      semester = row[3]
      course = row[4]
      session = row[5]
      jsonstr = jsonstr + '{"studentid":"'+str(id)+'", "name":"'+name+'", "rollno":"'+str(rollno)+'", "semester":"'+str(semester)+'", "course":"'+str(course)+'", "session":"'+str(session)+'"},'

   if found == True:
      jsonstr = jsonstr[:-1] + ''

   jsonstr = jsonstr + ']'

   return str(jsonstr)

@app.route('/appgetattendance', methods = ['GET', 'POST'])
def appgetattendance():
   q = ""
   if request.method == 'POST' or request.method == 'GET':
      q = request.args.get("studentid")

   conn = sqlite3.connect("faces.db")
   sql = "SELECT * from attendance WHERE studentid = " + str(q) + ""

   cursor = conn.execute(sql)
   #data = cursor.fetchall()

   found = False

   jsonstr = '['
   for row in cursor:
      found = True
      id = row[0]
      sid = row[1]
      attended = row[2]
      attenddate = row[3]

      jsonstr = jsonstr + '{"id":"'+str(id)+'", "studentid":"'+str(sid)+'", "attended":"'+str(attended)+'", "attenddate":"'+str(attenddate)+'"},'

   if found == True:
      jsonstr = jsonstr[:-1] + ''

   jsonstr = jsonstr + ']'

   return str(jsonstr)

@app.route('/appgetattendancewithdate', methods = ['GET', 'POST'])
def appgetattendancewithdate():
   q = ""
   if request.method == 'POST' or request.method == 'GET':
      studentid = request.args.get("studentid")
      fromdate = request.args.get("fromdate")
      todate = request.args.get("todate")
   conn = sqlite3.connect("faces.db")
   row = (studentid,fromdate,todate)
   cursor = conn.execute("SELECT * FROM attendance WHERE studentid = ? AND attenddate >= ? and attenddate <= ? ORDER BY attenddate", row)

   found = False

   jsonstr = '['
   for row in cursor:
      found = True
      id = row[0]
      sid = row[1]
      attended = row[2]
      attenddate = row[3]

      jsonstr = jsonstr + '{"id":"'+str(id)+'", "studentid":"'+str(sid)+'", "attended":"'+str(attended)+'", "attenddate":"'+str(attenddate)+'"},'

   if found == True:
      jsonstr = jsonstr[:-1] + ''

   jsonstr = jsonstr + ']'

   return str(jsonstr)

@app.route('/appdeletestudent', methods = ['GET', 'POST'])
def appdeletestudent():
   if request.method == 'POST' or request.method == 'GET':
      id = request.args.get("studentid")
      conn = sqlite3.connect("faces.db")
      conn.execute("PRAGMA foreign_keys = ON")
      row = (id,)
      conn.execute("DELETE FROM student WHERE id=?", row)
      conn.commit()
      if os.path.exists("static/train/"+str(id)):
         shutil.rmtree("static/train/"+str(id))
   return "1"

@app.route('/appimages', methods=['GET', 'POST'])
def appimages():
   jsonstr = '['
   found = False
   if request.method == 'POST' or request.method == 'GET':
      id = request.args.get('studentid')
      dir = 'static/train/'+str(id)
      
      if os.path.exists(dir):
         found = True
         files = os.listdir(dir)
         for f in files:
            f = f.replace(' ', '%20')
            jsonstr = jsonstr + '{"id":"'+str(id)+'", "name":"' + str(f) + '"},'

   if found == True:
      jsonstr = jsonstr[:-1] + ''
   jsonstr = jsonstr + ']'
         
   return jsonstr

@app.route('/appaddimages', methods=['GET', 'POST'])
def appaddimages():
   if request.method == 'POST' or request.method == 'GET':
      f = request.files['fileToUpload']
      id = request.args.get('studentid')
      dir = 'static/train/'+str(id)
      if not os.path.exists(dir):
         os.makedirs(dir)
      
      filename = dir+'/'+str(datetime.datetime.now().strftime("%Y%m%d_%H%M%S"))+'.jpg'
      f.save('static/temp.jpg')
      img = cv2.imread('static/temp.jpg')

      if getfacecount(img) == 0:
         return '[{"success" : "0","error" : "Error : no face found"}]'
      elif getfacecount(img) >= 2:
         return '[{"success" : "0","error" : "Error : more than face found"}]'

      cv2.imwrite(filename, getfacefromimage(img))
      return '[{"success" : "1","error" : "no error"}]'

@app.route('/appdeleteimage', methods=['GET', 'POST'])
def appdeleteimage():
   if request.method == 'POST' or request.method == 'GET':
      id = request.args.get('studentid')
      file = request.args.get('file')
      filename = 'static/train/'+str(id)+'/'+file
      if os.path.exists(filename):
         os.remove(filename)

         return '1'
      else:
         return '0'

@app.route('/appknntrain', methods=['GET', 'POST'])
def appknntrain():
   #train.train("static/train", model_save_path="trained_knn_model.clf", n_neighbors=2)
   t1 = threading.Thread(target = starttraining, args=())
   t1.start()
   t1.join()
   return '1'

#--------------------------ANDROID APP CODE END------------------------#

#--------------------------WEB PAGE CODE------------------------------#

#FIRST PAGE
@app.route('/')
def index():
   userid = request.cookies.get('userid')
   if userid == None:
      return render_template('index.html')
   else:
      return render_template('redirect.html', page = 'adminpanel')

#LOGIN PAGE
@app.route('/login')
def login():
   userid = request.cookies.get('userid')

   #check if logged in
   if userid != None:
      return render_template('adminpanel.html')

   return render_template('login.html')

@app.route('/loginsubmit', methods = ['GET', 'POST'])
def loginsubmit():
   if request.method == 'POST':
      username = request.form['username']
      password = request.form['password']
      ousername = ""
      opassword = ""

      if username != "admin":
         return render_template('login.html',error = 'Only Admin can login here')

      conn = sqlite3.connect('faces.db')
      c = conn.execute('SELECT * from admin WHERE username = ?', (username,))
      for row in c:
         ousername = row[0]
         opassword = row[1]

      resp = make_response(render_template('adminpanel.html'))

      if opassword == password:
         expire_date = datetime.datetime.now()
         expire_date = expire_date + datetime.timedelta(days=90)
         resp = make_response(render_template('redirect.html', page = 'adminpanel'))
         resp.set_cookie('userid',username, expires =  expire_date)
         return resp
         #return "Logged in <META http-equiv=\"refresh\" content=\"1;URL=adminpanel\">"
      else:
         return render_template('login.html',error = 'Something is wrong, Not Logged')
      #return "Username = %s AND Password = %s" % (username, password)

@app.route('/adminpanel')
def adminpanel():
   userid = request.cookies.get('userid')

   # check if logged in
   if userid == None:
      return render_template('redirect.html', page = 'login')

   return render_template('adminpanel.html', userid = userid)

@app.route('/logout')
def logout():
   expire_date = datetime.datetime.now()
   expire_date = expire_date - datetime.timedelta(days=1)
   resp = make_response(render_template('redirect.html', page = 'login'))
   resp.set_cookie('userid', '', expires=expire_date)
   return resp
   #return "<META http-equiv=\"refresh\" content=\"1;URL=login\">"

@app.route('/addstudentform')
def addstudentform():
   return render_template('addstudent.html')

#Add student from Webpage
@app.route('/addstudentformsubmit', methods = ['GET', 'POST'])
def addstudentformsubmit():
   if request.method == 'POST' or request.method == 'GET':
      #f = request.files['fileToUpload']
      name = request.form['name']
      rollno = request.form['rollno']
      semester = request.form['semester']
      course = request.form['course']
      session = request.form['session']
      #f.save('file/' + secure_filename(f.filename))
      try:
         conn = sqlite3.connect("faces.db")
         row = (name,rollno,semester,course,session,)
         conn.execute("INSERT INTO student (name,rollno,semester,course,session)values(?,?,?,?,?)", row)
         conn.commit()

         # get last id
         conn = sqlite3.connect('faces.db')
         lastid = 0
         query = "select seq as i from sqlite_sequence where name='student'";
         c = conn.execute(query);
         for row in c:
            lastid = row[0]
         # get last id end

         return 'Successfully Saved <a href="addimage?id=' + str(lastid) + '">Add Face</a>'
      except sqlite3.Error as e:
         return str(e)

@app.route('/addimage', methods=['GET', 'POST'])
def addimage():
   if request.method == 'POST' or request.method == 'GET':
      id = request.args.get('id')
      return render_template("addimage.html", id = id)

@app.route('/addimagesubmit', methods=['GET', 'POST'])
def addimagesubmit():
   if request.method == 'POST' or request.method == 'GET':
      f = request.files['fileToUpload']
      id = request.args.get('id')
      dir = 'static/train/'+str(id)
      if not os.path.exists(dir):
         os.makedirs(dir)
      filename = dir+'/'+str(datetime.datetime.now().strftime("%Y%m%d_%H%M%S"))+'.jpg'
      f.save('static/temp.jpg')
      img = cv2.imread('static/temp.jpg')

      if getfacecount(img) == 0:
         return '[{"success" : "0","error" : "Error : no face found"}]'
      elif getfacecount(img) >= 2:
         return '[{"success" : "0","error" : "Error : more than 1 face found"}]'
      
      cv2.imwrite(filename, getfacefromimage(img))
      return 'Successfully Added Image'

@app.route('/viewimage', methods=['GET', 'POST'])
def viewimage():
   if request.method == 'POST' or request.method == 'GET':
      id = request.args.get('id')
      dir = 'static/train/'+str(id)
      if os.path.exists(dir):
         files = os.listdir(dir)
         return render_template("viewimage.html", id = id, files = files)
      else:
         return render_template("viewimage.html", id = id)

@app.route('/deleteimage', methods=['GET', 'POST'])
def deleteimage():
   if request.method == 'POST' or request.method == 'GET':
      id = request.args.get('id')
      file = request.args.get('file')
      filename = 'static/train/'+str(id)+'/'+file
      if os.path.exists(filename):
         os.remove(filename)
         return 'Deleted'
      else:
         return 'Already Deleted'

#TRAINING
@app.route('/knntrain', methods=['GET', 'POST'])
def knntrain():
   t1 = threading.Thread(target = starttraining, args=())
   t1.start()
   t1.join()
   return 'Trained Successfully'

@app.route('/searchstudentform', methods = ['GET', 'POST'])
def searchstudentform():
   q = ""
   if request.method == 'POST' or request.method == 'GET':
      w = request.args.get("with")
      q = request.args.get("q")

   conn = sqlite3.connect("faces.db")

   if w == 'studentid':
      sql = "SELECT * from student WHERE id LIKE '%" + str(q) + "%'"
   elif w == 'name':
      sql = "SELECT * from student WHERE name LIKE '%" + str(q) + "%'"
   elif w == 'rollno':
      sql = "SELECT * from student WHERE rollno LIKE '%" + str(q) + "%'"
   elif w == 'semester':
      sql = "SELECT * from student WHERE semester LIKE '%" + str(q) + "%'"
   elif w == 'course':
      sql = "SELECT * from student WHERE course LIKE '%" + str(q) + "%'"
   elif w == 'session':
      sql = "SELECT * from student WHERE session LIKE '%" + str(q) + "%'"
   else:
      sql = "SELECT * from student"

   cursor = conn.execute(sql)

   data = cursor.fetchall()
   '''
   for row in cursor:
      id = row[0]
      name = row[1]
      rollno = row[2]
      semester = row[3]
      course = row[4]
      session = row[5]
      print(id)
   '''
   return render_template('searchstudentform.html',data = data, time =  time.time())

@app.route('/updatestudentform', methods = ['GET', 'POST'])
def updatestudentform():
   id = ""
   if request.method == 'POST' or request.method == 'GET':
      id = request.args.get("id")
      conn = sqlite3.connect("faces.db")
      cursor = conn.execute(
         "SELECT * from student WHERE id=" + str(id) + "")
      data = cursor.fetchall()

   return render_template('updatestudentform.html',id = id, data = data, time = time.time())

@app.route('/updatestudentformsubmit', methods = ['GET', 'POST'])
def updatestudentformsubmit():
   if request.method == 'POST' or request.method == 'GET':
      id = request.form["id"]
      name = request.form["name"]
      rollno = request.form["rollno"]
      semester = request.form["semester"]
      course = request.form["course"]
      session = request.form["session"]
      row = (name, rollno, semester, course, session, id)
      conn = sqlite3.connect("faces.db")
      conn.execute("UPDATE student SET name=?, rollno=?, semester=?, course=?, session=? WHERE id=?", row)
      conn.commit()

   return "Updated <META http-equiv=\"refresh\" content=\"1;URL=updatestudentform?id=" + str(id) + "\">"

@app.route('/deletestudentsubmit', methods = ['GET', 'POST'])
def deletestudentsubmit():
   if request.method == 'POST' or request.method == 'GET':
      id = request.args.get("id")
      conn = sqlite3.connect("faces.db")
      conn.execute("PRAGMA foreign_keys = ON")
      row = (id,)
      conn.execute("DELETE FROM student WHERE id IN (?)", row)
      conn.commit()
      if os.path.exists("static/train/"+str(id)):
         shutil.rmtree("static/train/"+str(id))
   return "Deleted<META http-equiv=\"refresh\" content=\"5;URL=searchstudentform\">"

@app.route('/viewattendanceform', methods = ['GET', 'POST'])
def viewattendanceform():
   studentid = "0"
   isWithDate = False
   if request.method == 'POST' or request.method == 'GET':
      studentid = request.args.get("studentid")
      fromdate = request.args.get("fromdate")
      todate = request.args.get("todate")
      totalclasses = request.args.get("totalclass")

   if fromdate != None:
      isWithDate = True

   print (isWithDate)

   try:
      conn = sqlite3.connect("faces.db")
      if isWithDate == True:
         row = (studentid, fromdate, todate,)
         cursor = conn.execute("SELECT * FROM attendance WHERE studentid = ? AND attenddate >= ? and attenddate <= ? ORDER BY attenddate", row)
      else:
         row = (studentid,)
         cursor = conn.execute("SELECT * FROM attendance WHERE studentid = ? ORDER BY attenddate",row)
      data = cursor.fetchall()

      total = data.__len__()
      try:
         percent = (100 / int(totalclasses)) * total
      except Exception as e:
         percent = 0

      return render_template('viewattendance.html',studentid=studentid, data = data, fromdate=fromdate, todate=todate, total=total, percent=percent)
   except sqlite3.Error as e:
      return str(e)

@app.route('/print', methods = ['GET', 'POST'])
def printform():
   studentid = "0"
   totalclasses = "0"
   studentname = ''

   if request.method == 'POST' or request.method == 'GET':
      studentid = request.args.get('studentid')
      totalclasses = request.args.get('total')
      try:
         conn = sqlite3.connect("faces.db")
         cursor = conn.execute("SELECT name,rollno,semester,session FROM student WHERE id = "+str(studentid))
         for row in cursor:
            studentname = row[0]
            studentrollno = row[1]
            studentsemester = row[2]
            studentsession = row[3]

         row = (studentid,)
         cursor = conn.execute("SELECT * FROM attendance WHERE studentid = ? ORDER BY attenddate", row)
         data = cursor.fetchall()
         total = data.__len__()
         try:
            percent = (100 / int(totalclasses)) * total
         except Exception as e:
            percent = 0
         return render_template('print.html', studentid=studentid, name = studentname, rollno = studentrollno, semester = studentsemester, session = studentsession, data=data, totalclass = totalclasses, totalattended = total, percentage = percent)
      except sqlite3.Error as e:
         return str(e)
   return render_template('print.html')

@app.route('/insertattendance', methods = ['GET', 'POST'])
def insertattendance():
   if request.method == 'POST' or request.method == 'GET':
      studentid = request.args.get("studentid")
      conn = sqlite3.connect("faces.db")
      row = (str(studentid), str(1), date.today())
      try:
         conn.execute("INSERT INTO attendance (studentid, attended,attenddate)values(?,?,?)", row)
      except sqlite3.Error as e:
         return '<h1>Already Attended Today</h1>'
      conn.commit()
   return "Inserted"

@app.route('/updateattendance', methods = ['GET', 'POST'])
def updateattendance():
   if request.method == 'POST' or request.method == 'GET':
      id = request.args.get("id")
      attended = request.args.get("attended")
      studentid = request.args.get("studentid")
      conn = sqlite3.connect("faces.db")
      row = (attended, id)
      conn.execute("UPDATE attendance SET attended=? WHERE id=?", row)
      conn.commit()
   return "Updated<META http-equiv=\"refresh\" content=\"0.1;URL=viewattendanceform?studentid=" + str(studentid) + "\">"

@app.route('/deleteattendance', methods = ['GET', 'POST'])
def deleteattendance():
   if request.method == 'POST' or request.method == 'GET':
      id = request.args.get("id")
      studentid = request.args.get("studentid")
      conn = sqlite3.connect("faces.db")
      row = (id)
      conn.execute("DELETE FROM attendance WHERE id=?", row)
      conn.commit()
   return "Updated<META http-equiv=\"refresh\" content=\"0.1;URL=viewattendanceform?studentid=" + str(studentid) + "\">"

@app.route('/takeattendanceform', methods = ['GET', 'POST'])
def takeattendanceform():
   return render_template('takeattendance.html')

@app.route('/takeattendanceformsubmit', methods = ['GET', 'POST'])
def takeattendanceformsubmit():
   if request.method == 'POST':
      f = request.files['fileToUpload']
      f.save('static/temp.jpg')

      full_file_path = 'static/temp.jpg'

      founduserid = 'null'
      foundname = 'null'
      foundrollno = 'null'
      foundsemester = 'null'
      foundcourse = 'null'
      foundsession = 'null'

      predictions = knn.predict(full_file_path, model_path="static/trained_knn_model.clf")
      founduserid = knn.getName(predictions)

      conn = sqlite3.connect('faces.db')
      row = (founduserid,)
      cursor = conn.execute("SELECT * from student WHERE id = ?",row)
      for row in cursor:
         foundname = row[1]
         foundrollno = row[2]
         foundsemester = row[3]
         foundcourse = row[4]
         foundsession = row[5]

   return render_template("takeattendancesubmit.html",studentid=founduserid, name=foundname, rollno=foundrollno, semester=foundsemester, course=foundcourse, session=foundsession)

#MANAGE ADMIN AND FACULTY MEMBERS
@app.route('/manage')
def manage():
   conn = sqlite3.connect("faces.db")
   sql = "SELECT * from admin"
   cursor = conn.execute(sql)
   data = cursor.fetchall()
   return render_template('manage.html', data = data)

@app.route('/adduser', methods = ['GET', 'POST'])
def adduser():
   if request.method == 'POST':
      username = request.form['username']
      password = request.form['password']

      try:
         conn = sqlite3.connect("faces.db")
         row = (username,password,)
         conn.execute("INSERT INTO admin (username,password)values(?,?)", row)
         conn.commit()
      except sqlite3.Error as e:
         return str(e)

   return render_template('redirect.html', page = 'manage')

@app.route('/updateuser', methods = ['GET', 'POST'])
def updateuser():

   ousername = request.args.get('username')
   success = ''

   if request.method == 'POST':
      userid = request.form['username']
      password = request.form['password']

      try:
         conn = sqlite3.connect("faces.db")
         row = (userid, password, ousername)
         conn.execute("UPDATE admin SET username = ?, password = ? WHERE username = ?", row)
         conn.commit()
         success = 'Successfully Updated'
      except sqlite3.Error as e:
         return str(e)

   return render_template('updateuser.html', username = ousername, success=success)

@app.route('/deleteuser', methods = ['GET', 'POST'])
def deleteuser():
   if request.method == 'GET':
      username = request.args.get('username')
      if username == 'admin':
         return "You can not delete admin"
      try:
         conn = sqlite3.connect("faces.db")
         row = (username,)
         conn.execute("DELETE FROM admin WHERE username = ?", row)
         conn.commit()
      except sqlite3.Error as e:
         return str(e)

   return render_template('redirect.html', page = 'manage')

if __name__ == '__main__':
   app.run(host = "192.168.43.29",debug = True)
