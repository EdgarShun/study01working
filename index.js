import React from 'react';
import {AppRegistry, StyleSheet, Text, View, NativeModules,Button} from 'react-native';

class study01 extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
        content: '你想问点啥？',
        }
    }

  render() {
    return (
     <View style={styles.container}>

              <View style={styles.buttonContainer}>
                    <Button
                      onPress={this.getAndroid.bind(this)}
                      title="Android原生"
                    />
              </View>



             <View style={styles.buttonContainer}>
               <Button
                 onPress={this.getAIUI.bind(this)}
                 title="AIUI"
               />
             </View>

             <View style={styles.buttonContainer}>
                <Button
                  onPress={this.getIat.bind(this)}
                  title="语音听写"
                 />
             </View>

             <View style={styles.buttonContainer}>
                <Button
                   onPress={this.getTta.bind(this)}
                   title="语音合成"
                 />
             </View>



             <Text style={styles.hello} >
             {this.state.content}
             </Text>

     </View>
    );
  }

  getAIUI()
  {
    NativeModules.Voice.getAIUI((result)=>{this.setState({content:result,});});
  }

  getIat()
  {
    NativeModules.Voice.getIat((result)=>{this.setState({content:result,});});
  }

  getTta()
  {
    NativeModules.Voice.getTta(this.state.content);
  }

  getAndroid()
  {
    NativeModules.Voice.getAndroid();
  }


}
var styles = StyleSheet.create({
 container: {
    flex: 1,
    justifyContent: 'center',
   },
   buttonContainer: {
       margin: 20
     },
  hello: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
});

AppRegistry.registerComponent('study01', () => study01);
