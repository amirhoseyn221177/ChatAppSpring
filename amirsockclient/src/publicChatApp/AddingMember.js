import React, { useState, Fragment} from "react"
import Axios from "axios"




const AddingMember = props => {
    const [groupId, setGroupId] = useState("")
    const [username, setUserName] = useState("")

    var addingToGroup = async () => {
        const resp = await Axios.post(`/restchat/addtogroup/${groupId}/${username}`)
        const data = await resp.data

        console.log(data)
    }


    return (
        <Fragment>

            <form>
                <input placeholder="please type the username you want to add" value={username} onChange={e => setUserName(e.target.value)} />
                <input placeholder="group Id" value={groupId} onChange={e => setGroupId(e.target.value)} />
                <button type="button" onClick={addingToGroup}>add</button>
            </form>

        </Fragment>
    )

}

export default AddingMember;