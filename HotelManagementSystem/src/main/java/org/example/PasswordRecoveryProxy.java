package org.example;

class PasswordRecoveryProxy implements PasswordRecoveryService {
    private RealPasswordRecovery realService;
    private Manager manager;

    public PasswordRecoveryProxy() {
        this.manager = Manager.getmanager();
        this.realService = new RealPasswordRecovery();
    }

    @Override
    public void recoverPassword(String adminEmail) {
        if (adminEmail.equals(manager.getRegisteredEmail())) {
            System.out.println("Email validated. Access granted.");
            realService.recoverPassword(adminEmail);
        } else {
            System.out.println("Access denied: Invalid email.");
        }
    }
}

