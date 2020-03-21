import React, { Component } from 'react';
import axios from 'axios';
class PlayerForm extends Component {

    constructor(props) {
        super(props);
        this.nickname = React.createRef();
        this.state = {
            errors: [],
            status: 'new',
            loading: false
        };

        this.submitForm = this.submitForm.bind(this);
        this.renderForm = this.renderForm.bind(this);
    }

    submitForm(e) {
        e.preventDefault();
        const { nickname } = this;
        const errors = [];
        if (nickname.current.value.length === 0) {
            errors.push({
                field: 'nickname',
                message: 'Nickname cannot be null'
            });
        }

        if(errors.length) {
            return this.setState({
                errors
            })
        }

        const body = JSON.stringify({
            nickname: nickname.current.value
        });

        axios({method: 'POST', url: 'https://pqg138n96h.execute-api.ap-southeast-2.amazonaws.com/dev/v1/players', data: body, crossDomain: true,
            headers: { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' }
        }).then(response => {
            this.setState({
                status: 'success'
            });

        }, (error) => {
            errors.push({
                field: 'server',
                message: 'Registration failed. '
            })
        }).then(() => {
            this.setState({
                errors
            })
        });
    }

    getErrors(field) {
        return this.state.errors.filter(error => {
            return error.field === field;
        });
    }

    renderForm() {
        const { nickname } = this;

        const nicknameErrors = this.getErrors('nickname');

        const serverError = this.getErrors('server');

        return (
            <form onSubmit={this.submitForm} className="container">
                <div className="row">
                    <div><span>FIXME display uuid, emailAddress, registrationDate and lastLoginDate</span></div>
                    <div className="col-lg-12">
                        <input placeholder="Nickname" type="text" className={`form-control mt-2 ${nicknameErrors.length ? 'error' : ''} `} ref={nickname} required/>
                        {nicknameErrors.map((error, index) => (
                            <div className="alert alert-danger" key={index}>
                                {error.message}
                            </div>
                        ))}
                    </div>
                    <div><span>FIXME display avatar selection and statistics and tournamentStatistics tables</span></div>

                    <div className="col-lg-12 mt-2 text-center">
                        <button type="submit" className="btn btn-default">Submit</button>
                        { serverError.length ? <div className="alert mt-2 alert-danger">{serverError.pop().message}</div> : null}
                    </div>
                </div>
            </form>
        );
    }

    render() {
        switch(this.state.status) {
            case 'loading': return (<i className="fa fa-loading fa-2x"></i>);
            case 'success': return (<div className="">Welcome, Commander!</div>);
            default:
                return this.renderForm();
        }
    }
}


export default PlayerForm;  // adds dispatch prop
