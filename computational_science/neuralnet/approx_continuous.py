import matplotlib.pyplot as plt
import numpy as np
import random
import tensorflow as tf
import tensorflow.keras as keras
from tensorflow.python.keras.callbacks import EarlyStopping

extension_factor = 1.5

# The mathematical function that should be approximated
def func(x):
    return pow(x, 3) - 6.0 * pow(x, 2) + 4.0 * x + 12.0

def plot_func(start=-7.5, stop=7.5, step=0.01):
    plt.axhline(0, color='black') # draw horizontal axis
    plt.axvline(0, color='black') # draw vertical axis
    plt.plot(np.arange(start, stop, step),
             [func(x) for x in np.arange(start, stop, step)])
    plt.show()

def generate_dataset(func, start, end, count, seed=42):
    random.seed(seed)
    data = {}

    for _ in range(count):
        vin = random.uniform(start, end)
        data[vin] = func(vin)

    d_in = sorted(data.keys())
    d_out = [data[x] for x in d_in]

    print(len(d_in))
    print(len(d_out))

    return d_in, d_out

def train_NN(dataset, x_from, x_to, use_bias=True, activation_func="relu", batch_size=128, layer_size=128, weight_initializer="random_normal"):
    model = tf.keras.models.Sequential()
    model.add(tf.keras.layers.Dense(1, input_shape=(1,)))
    model.add(tf.keras.layers.Activation('linear'))
    model.add(tf.keras.layers.Dense(layer_size, use_bias=use_bias, kernel_initializer=weight_initializer))
    model.add(tf.keras.layers.Activation(activation_func))
    model.add(tf.keras.layers.Dense(layer_size, use_bias=use_bias, kernel_initializer=weight_initializer))
    model.add(tf.keras.layers.Activation(activation_func))
    model.add(tf.keras.layers.Dense(1))

    optim = keras.optimizers.Adam(learning_rate=0.005, beta_1=0.90, beta_2=0.99, amsgrad=True)

    loss = "mean_absolute_percentage_error"
    model.compile(optimizer=optim, loss=loss, metrics=[loss])

    es = EarlyStopping(monitor='loss', mode='auto', verbose=1, patience=50, restore_best_weights=True)
    # you can increase epochs if you have a PC without GPU
    model.fit(np.array(dataset[0]), np.array(dataset[1]), epochs=1000, batch_size=batch_size, callbacks=[es])#, validation_split=1.0)

    predicts_x = np.arange(x_from*extension_factor, x_to*extension_factor, 0.01)
    predicts = model.predict(predicts_x)

    return predicts

def plot(fig, ax, x, y, label, color="b", xlim=(-3,6), ylim=(-10, 20), sgidx=(1, 1, 1)):
    if ax is None:
        if sgidx is None:
            ax = fig.add_subplot(1, 1, 1)
        else:
            ax = fig.add_subplot(sgidx[0], sgidx[1], sgidx[2])

    # Move left y-axis and bottim x-axis to centre, passing through (0,0)
    ax.spines['left'].set_position("zero")
    ax.spines['bottom'].set_position("zero")

    # Eliminate upper and right axes
    ax.spines['right'].set_color('none')
    ax.spines['top'].set_color('none')

    # Show ticks in the left and lower axes only
    ax.xaxis.set_ticks_position('bottom')
    ax.yaxis.set_ticks_position('left')
    ax.set_xlim(xlim)
    ax.set_ylim(ylim)

    ax.plot(x, y, color+'o', markersize=2, label=label)

    return fig, ax

def approximate_and_plot(samples=30, batch_size=50, sgidx=None,
                         activation="relu", use_bias=True,
                         hidden_layer_size=1024, weight_initializer="glorot_uniform"):
    #random_state = 42
    #np.random.seed(random_state)
    #tf.set_random_seed(random_state)

    x_from = -10.0
    x_to = 10.0
    y_from = -1000.0
    y_to = 1000.0

    dataset = generate_dataset(func, x_from, x_to, samples)

    if (sgidx is None) or (sgidx[2] == 1):
        approximate_and_plot.fig_zoom = plt.figure(figsize=(15, 15))
        approximate_and_plot.fig_full = plt.figure(figsize=(15, 15))
    fig_zoom = approximate_and_plot.fig_zoom
    fig_full = approximate_and_plot.fig_full
    fig_zoom, ax_zoom = plot(fig_zoom, None, np.arange(x_from*extension_factor, x_to*extension_factor, 0.01),
                             [func(x) for x in np.arange(x_from*extension_factor, x_to*extension_factor, 0.01)],
                             "Exact Function", sgidx=sgidx)
    fig_full, ax_full = plot(fig_full, None, np.arange(x_from*extension_factor, x_to*extension_factor, 0.01),
                             [func(x) for x in np.arange(x_from*extension_factor, x_to*extension_factor, 0.01)],
                             "Exact Function", sgidx=sgidx, xlim=(x_from*extension_factor, x_to*extension_factor),
                             ylim=(y_from*extension_factor, y_to*extension_factor))

    nn_data = train_NN(dataset, x_from, x_to, batch_size=batch_size, activation_func=activation,
                        use_bias=use_bias, layer_size=hidden_layer_size, weight_initializer=weight_initializer)

    fig_zoom, ax_zoom = plot(fig_zoom, ax_zoom, np.arange(x_from*extension_factor, x_to*extension_factor, 0.01), nn_data,
                             "NN aprox.", color="g")
    fig_zoom, ax_zoom = plot(fig_zoom, ax_zoom, dataset[0], dataset[1], "learning dataset", color="r")

    fig_full, ax_full = plot(fig_full, ax_full, np.arange(x_from*extension_factor, x_to*extension_factor, 0.01), nn_data,
                             "NN aprox.", color="g", xlim=(x_from*extension_factor, x_to*extension_factor),
                             ylim=(y_from*extension_factor, y_to*extension_factor))
    fig_full, ax_full = plot(fig_full, ax_full, dataset[0], dataset[1], "learning dataset", color="r",
                             xlim=(x_from*extension_factor, x_to*extension_factor),
                             ylim=(y_from*extension_factor, y_to*extension_factor))

    if (sgidx is not None) and (sgidx[2] <= sgidx[1]):
        ax_zoom.set_title("samples: %s" % samples)
        ax_full.set_title("samples: %s" % samples)
    if (sgidx is not None) and (sgidx[2] % sgidx[1] == 1):
        ax_zoom.set_ylabel("batchsize: %s" % batch_size, rotation=90, size='large')
        ax_full.set_ylabel("batchsize: %s" % batch_size, rotation=90, size='large')

    if (sgidx is None) or (sgidx[2] == sgidx[0]*sgidx[1]):
        plt.legend()
        plt.show()


def main():
    #plot_func(-2, 6, 0.01)
    # approximate_and_plot(samples=256, batch_size=128, activation="relu", use_bias=True,
    #                      hidden_layer_size=256, weight_initializer="zero")
    
    #approximate_and_plot(samples=256, batch_size=128, activation="relu", use_bias=True,
    #                     hidden_layer_size=256, weight_initializer="glorot_uniform")
    
    samples = [30, 100, 250]
    batch_sizes = [50, 100, 200]
    
    for bsi, bs in enumerate(batch_sizes):
       for ssi, ss in enumerate(samples):
           print(len(samples)*ssi + bsi + 1)
           approximate_and_plot(samples=ss, batch_size=bs, sgidx=(len(batch_sizes), len(samples),  len(samples)*bsi + ssi + 1),
                                activation="relu", use_bias=True, hidden_layer_size=1024,
                                weight_initializer="glorot_uniform")



if __name__ == "__main__":
    main()












