import io

from tensorflow.keras import models
from tensorflow.keras import layers
from tensorflow.keras.datasets import mnist
from tensorflow.keras.utils import to_categorical

import tkinter as tk
from PIL import Image, ImageOps, ImageDraw
import numpy as np

from scipy.ndimage.interpolation import shift

import matplotlib.pyplot as plt

def shift_image(image, dx, dy):
    return shift(image, [dy, dx], cval=0, mode="constant")

def augment_dataset(x_train, y_train, x_test, y_test):
    x_train_augmented = [image for image in x_train]
    y_train_augmented = [label for label in y_train]
    x_test_augmented = [image for image in x_test]
    y_test_augmented = [label for label in y_test]

    for dx, dy in ((1,0), (-1,0), (0,1), (0,-1)):
     for image, label in zip(x_train, y_train):
             x_train_augmented.append(shift_image(image, dx, dy))
             y_train_augmented.append(label)
      
     for image, label in zip(x_test, y_test):
             x_test_augmented.append(shift_image(image, dx, dy))
             y_test_augmented.append(label)
    return np.array(x_train_augmented), np.array(y_train_augmented), np.array(x_test_augmented), np.array(y_test_augmented) 

def train():
    # 4 numpy's array
    (train_images, train_labels), (test_images, test_labels) = mnist.load_data()

    #augment dataset
    #train_images, train_labels, test_images, test_labels = augment_dataset(train_images, train_labels, test_images, test_labels)

    network = models.Sequential()
    # linear stack of layers
    network.add(layers.Dense(128, activation='relu', input_shape=(28 * 28,)))
    #network.add(layers.GaussianNoise(0.15))
    network.add(layers.Dense(10, activation='softmax'))
    # digit probability
    network.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])

    images_and_labels = list(zip(train_images, train_labels))
    bynumber = {x:[] for x in range(10)}
    for index, (image, label) in enumerate(images_and_labels):
        bynumber[label].append(image)
    fig = plt.figure(figsize=(15, 15))
    for y in range(10):
        for x in range(10):
            image = bynumber[x][y]
            plt.subplot(10, 10, 10*y + x + 1)
            plt.axis('off')
            fig.set_facecolor("gray")
            plt.imshow(image, cmap=plt.cm.gray_r, interpolation='nearest')

    plt.show()

    print("Using {} training images".format(train_images.shape[0]))
    train_images = train_images.reshape((train_images.shape[0], 28 * 28))
    train_images = train_images.astype('float32') / 255
    # we want to work with float in [0,1]
    print("Using {} testing images".format(test_images.shape[0]))
    test_images = test_images.reshape((test_images.shape[0], 28 * 28))
    test_images = test_images.astype('float32') / 255

    train_labels = to_categorical(train_labels)
    test_labels = to_categorical(test_labels)
    network.fit(train_images, train_labels, epochs=3, batch_size=128)
    test_loss, test_acc = network.evaluate(test_images, test_labels)

    print("Testing loss: %s" % test_loss)
    print("Testing Accuracy: %s" % test_acc)
    print(network.summary())
    return network

def infer_image(model, image):
    data = np.array([np.asarray(image).flatten().astype('float32') / 255])

    prediction = model.predict(data)
    print(prediction)
    number = np.argmax(prediction)
    confidence = prediction[:,number][0];
    print(number)
    print(confidence)
    return number, confidence 


def preprocess_image(img):
    img2 = img.convert('L')
    img2.thumbnail((28, 28), Image.ANTIALIAS)
    
    return img2

def main():
    global painting
    painting = False
    model = train()

    canvas_width = 280
    canvas_height = 280

    image = Image.new("RGB", (canvas_width, canvas_height),  (255, 255, 255))
    draw = ImageDraw.Draw(image)

    def start_paint(event):
        global painting
        #print("Start drawing")
        painting = True

    def paint(event):
        global painting
        if not painting:
            return
        x1, y1 = (event.x - 12), (event.y - 12)
        x2, y2 = (event.x + 12), (event.y + 12)
        w.create_oval(x1, y1, x2, y2, fill="black")
        draw.ellipse([x1, y1, x2, y2], (0, 0, 0))

    def stop_paint(event):
        global painting
        #print("Stop drawing")
        painting = False
        #image.show()
        img = preprocess_image(image)

        # display the transformed image
        img = ImageOps.invert(img)
        plt.imshow(img, cmap=plt.cm.gray_r, interpolation='nearest')
        plt.show()

        number, confidence = infer_image(model, img)
        text_var.set(str(number))
        confidence_var.set(str(confidence))

    def clear(event):
        w.delete("all")
        draw.rectangle((0, 0, canvas_width, canvas_height), fill=(255, 255, 255, 0))
        text_var.set("none")
        confidence_var.set("")

    master = tk.Tk()
    master.resizable(False, False)
    master.title("Handwritten digit recognition")
    w = tk.Canvas(master,
               width=canvas_width,
               height=canvas_height)
    w.pack(expand=tk.NO, fill=tk.BOTH)
    w.bind("<ButtonPress-1>", start_paint)
    w.bind("<Motion>", paint)
    w.bind("<ButtonRelease-1>", stop_paint)
    w.bind("<ButtonRelease-3>", clear)

    message = tk.Label(master, text="Press and Drag the mouse to draw\nRight-click to clear!")
    message.pack(side=tk.BOTTOM)
    text_var = tk.StringVar(master)
    text_var.set("no number yet!")
    infered_number = tk.Label(master, textvariable=text_var)
    infered_number.pack(side=tk.BOTTOM)
    confidence_var = tk.StringVar(master)
    confidence_var.set("")
    infered_confidence = tk.Label(master, textvariable=confidence_var)
    infered_confidence.pack(side=tk.BOTTOM)

    tk.mainloop()


if __name__ == "__main__":
    main()