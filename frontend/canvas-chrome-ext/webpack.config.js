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
        studentContent: './src/scripts/student-content.jsx',
        studentInject: './src/scripts/student-inject-script.jsx',
        instructorContent: './src/scripts/instructor-content.jsx',
        instructorInject: './src/scripts/instructor-inject-script.jsx',
        compilationStatus: './src/scripts/compilation-notification.jsx',
    },
    output: {
        path: path.resolve(__dirname, 'dist'),
        filename: '[name].js',
    },

    optimization: {
        minimize: false
    },

    module: {
        rules: [{
            test: /\.(js|jsx)$/,
            exclude: /node_modules/,
            use: {
                loader: 'babel-loader',
                options: {
                    presets: ['@babel/preset-env', '@babel/preset-react'],
                }
            }
        }],
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
            chunks: ['compilationStatus']
        }),
        new CopyPlugin({
            patterns: [
                {from: "public"},
            ],
        }),
    ],
};