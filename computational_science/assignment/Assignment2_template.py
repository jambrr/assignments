#!/usr/bin/env python
# coding: utf-8

# In[1]:


import numpy as np
import pandas as pd
import random
from datetime import datetime
import tensorflow as tf
from tensorflow.keras import models 
from tensorflow.keras import layers
from sklearn.model_selection import train_test_split


# In[2]:


# Python RNG
import random
random.seed(42)

# Numpy RNG
import numpy as np
np.random.seed(42)

# TF RNG
from tensorflow.python.framework import random_seed
random_seed.set_seed(42)


# In[3]:


def load_dataset():
    data = pd.read_csv("dataset/data.csv", header=None)
    labels = pd.read_csv("dataset/labels.csv", header=None)
    labels["label"] = labels.apply(lambda x: x[0] > 0.5, axis=1)
    labels.drop(labels.columns[:2], axis=1, inplace=True)
    print(data.head())

    training_data, test_data, training_labels, test_labels = train_test_split(data, labels, test_size=0.2, random_state=42)

    print(training_data.describe())
    print(training_labels.describe())
    print(test_data.describe())
    print(test_labels.describe())

    return training_data, training_labels, test_data, test_labels


# In[4]:


def train_model(dataset):
    tf.keras.backend.clear_session()
    model = models.Sequential() # linear sequence of layers
    model.add(layers.Dense(100, activation='relu', input_shape=(6,)))
    #model.add(layers.Dropout(0.0))
    model.add(layers.Dense(1, activation='sigmoid')) # output probability 
    opt = tf.keras.optimizers.Adam(learning_rate=0.01)
    model.compile(optimizer=opt, loss='binary_crossentropy', metrics=['accuracy'])

    history = model.fit(dataset[0][:7000], dataset[1][:7000], epochs=20, batch_size=128)
    print(model.summary)

    return model


# In[5]:


# DO NOT TRY TO CHANGE THIS METHOD!
def grade_model(model, dataset, silent=False):
    lower = 0.75
    upper = 0.92
    scale = 20.0
    bonus = 2.0
    overfitting_margin = 0.5  # 0.5 percent are allowed!

    train_data, train_label, test_data, test_labels = dataset
    train_loss, train_acc = model.evaluate(train_data, train_label)
    test_loss, test_acc = model.evaluate(test_data, test_labels)
    print("train_acc:", train_acc)
    print("test_acc:", test_acc)
    grade = (test_acc - lower) / (upper - lower)
    overfitting = abs(test_acc - train_acc)
    overfitting_penalty = max(overfitting*100.0 - overfitting_margin, 0.0) * 0.5  # overfitting and underfitting will be punished by 0.5 point/percent
    grade = min(grade * scale, scale + bonus)      # you can get up to 2 points bonus for a high accuracy
    grade = max(grade - overfitting_penalty, 0.0)  # but it will be cut down if overfitting/underfitting is present!
    if not silent:
        print("Accuracy  -  test: %s; training: %s; overfitting: %s" % (test_acc, train_acc, overfitting))
        print("Grade: %s (/%s + %s)  (overfitting penalty: %s)" % (grade, scale, bonus, overfitting_penalty))
    return grade


# In[6]:


def main():
    start_time = datetime.now()
    # DO NOT MODIFY THIS CODE!
    dataset = load_dataset()
    print("dataset loaded %s" % (datetime.now() - start_time))
    start_time = datetime.now()

    model = train_model(dataset)
    grade_model(model, dataset)
    print("Done %s" % (datetime.now() - start_time))


# In[7]:


if __name__ == "__main__":
    main()


# In[ ]:




