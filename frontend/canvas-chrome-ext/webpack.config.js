/**
 * Webpack for building the project and creating static
 * versions of the components we need to display on the webpage and
 * placing them in the dist folder. The names created here are referenced in
 * manifest.json
 *
 * @type {path.PlatformPath | path}
 */

const path = require('path');
const HtmlWebpackPlugin = require("html-webpack-plugin");
const CopyPlugin = require("copy-webpack-plugin");

module.exports = {
    entry: {
        popup: './src/popup.jsx',
        background: './src/background.jsx',
        'student-content': './src/scripts/student-content.jsx',
        'student-inject-script': './src/scripts/student-inject-script.jsx',
        'instructor-content': './src/scripts/instructor-content.jsx',
        'instructor-inject-script': './src/scripts/instructor-inject-script.jsx',
        'compilation-notification': './src/scripts/compilation-notification.jsx',
    },
    output: {
        path: path.resolve(__dirname, 'dist'),
        filename: '[name].js',
    },

    optimization: {
        minimize: false
    },

    module: {
        rules: [
            {
                test: /\.(js|jsx)$/,
                exclude: /node_modules/,
                use: {
                    loader: 'babel-loader',
                    options: {
                        presets: ['@babel/preset-env', '@babel/preset-react'],
                    }
                }
            },
            {
                test: /\.hmtl$/,
                use: ['html-loader']
            },
            {
              test: /\.(jpg|png|jpeg|svg|gif)$/,
              use: [
                  {
                      loader: 'file-loader',
                      options: {
                          name: '[name].[ext]',
                          outputPath: 'img/',
                          publicPath: 'img/'
                      }
                  }
              ]
            }

        ],
    },

    plugins: [
        new HtmlWebpackPlugin({
            template: './src/html/popup.html',
            filename: 'popup.html',
            chunks: ['popup']
        }),
        new HtmlWebpackPlugin({
            template: './src/html/compilation-notification.html',
            filename: 'compilation-notification.html',
            chunks: ['compilation-notification']
        }),
        new CopyPlugin({
            patterns: [
                { from: "public" },
            ],
        }),
    ],
};