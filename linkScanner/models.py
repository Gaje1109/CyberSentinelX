from django.db import models

# Create your models here.

class Link(models.Model):

    def __str__(self):  #to display the link name directly instead of "link(1)"
        return self.link

    link = models.CharField(max_length=5000)
    status = models.CharField(max_length=100)


class Question(models.Model):
    question_text = models.CharField(max_length=200)
    correct_answer = models.CharField(max_length=200)
    def __str__(self):
        return self.question_text
