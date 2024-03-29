import React from 'react';
import {BrowserRouter,Route,Switch} from "react-router-dom"
import './App.css';
import PublicMessage from './publicChatApp/publicChatApp';
import PrivateMessage from './PrivateMessage/PrivateMessage';
import CreateGroupChat from './publicChatApp/CreateGroupChat';
import CreateUser from './CreateUser';
import AddingMember from './publicChatApp/AddingMember';
import SignIn from './Login';
import encryption from './encryption';

function App() {
  return (
  <BrowserRouter>
      <Switch>
      <Route exact path="/privatechat/:username/:otheruser" component={PrivateMessage}/>
      <Route exact path="/publicchat" component={PublicMessage}/>
      <Route exact path="/creategroup" component={CreateGroupChat}/>
      <Route exact path="/createuser" component={CreateUser}/>
      <Route exact path="/adding" component={AddingMember}/>
      <Route exact path="/login" component={SignIn}/>
      <Route path="/" component={encryption}/>
    </Switch>
  </BrowserRouter>
  
  );
}

export default App;
