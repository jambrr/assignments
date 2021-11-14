#!/usr/bin/env python
# coding: utf-8

# In[1]:


# Python RNG
import random
random.seed(42)

# Numpy RNG
import numpy as np
np.random.seed(42)

# TF RNG
from tensorflow.python.framework import random_seed
random_seed.set_seed(42)

# !pip3 install matplotlib
from matplotlib import pyplot as plt


# In[2]:


import tensorflow as tf
from tensorflow.keras import models 
from tensorflow.keras import layers
from tensorflow.keras.datasets import mnist
# 4 numpy's array
(train_images, train_labels), (test_images, test_labels) = mnist.load_data()

# preprocessing
train_images = train_images.reshape((60000, 28 * 28))
train_images = train_images.astype('float32') / 255 # we want to work with float in [0,1]
test_images = test_images.reshape((10000, 28 * 28))
test_images = test_images.astype('float32') / 255

from tensorflow.keras.utils import to_categorical
train_labels = to_categorical(train_labels)
test_labels = to_categorical(test_labels)


# In[3]:


# exercise1
#all_lr = [0.0001, 0.001, 0.005, 0.01, 0.1]
#all_history = []
#all_test_acc = []
#for lr in all_lr:
#    print("\nlr: ", lr)
#    tf.keras.backend.clear_session()
#    network = models.Sequential() # linear sequence of layers
#    network.add(layers.Dense(512, activation='relu', input_shape=(28 * 28,)))
#    network.add(layers.Dense(10, activation='softmax')) # digit probability 
#    opt = tf.keras.optimizers.Adam(learning_rate=lr, beta_1=0.9, beta_2=0.99, amsgrad=True)
#    network.compile(optimizer=opt, loss='categorical_crossentropy', metrics=['accuracy'])
#
#    history = network.fit(train_images, train_labels, epochs=10, batch_size=128)
#    all_history.append(history.history)
#    test_loss, test_acc = network.evaluate(test_images, test_labels)
#    all_test_acc.append(test_acc)

all_epochs = [1000, 5000, 10000, 30000, 60000]
all_history = []
all_test_acc = []
for epoch in all_epochs:
    print("\nepoch: ", epoch)
    tf.keras.backend.clear_session()
    network = models.Sequential() # linear sequence of layers
    network.add(layers.Dense(512, activation='relu', input_shape=(28 * 28,)))
    network.add(layers.Dense(10, activation='softmax')) # digit probability 
    opt = tf.keras.optimizers.Adam(learning_rate=0.005, beta_1=0.9, beta_2=0.99, amsgrad=True)
    network.compile(optimizer=opt, loss='categorical_crossentropy', metrics=['accuracy'])

    history = network.fit(train_images, train_labels, epochs=epoch, batch_size=128)
    all_history.append(history.history)
    test_loss, test_acc = network.evaluate(test_images, test_labels)
    all_test_acc.append(test_acc)


# In[6]:


# plot training loss
for history in all_history:
    plt.plot(history['loss'])
plt.title('training loss')
plt.ylabel('loss')
plt.xlabel('epoch')
plt.legend(all_epochs, loc='upper right')
plt.show()


# In[8]:


# plot testing accuracy
plt.plot(all_epochs, all_test_acc, '-o')
for i in range(len(all_epochs)):
    plt.annotate(all_epochs[i], (all_epochs[i], all_test_acc[i]))
plt.title('testing accuracy')
plt.ylabel('accuracy')
plt.xlabel('batch')
plt.show()


# In[ ]:




